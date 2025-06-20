

// ==========================================================================
// Cloud Functions para Sisvita Backend Lógico con Firebase
// ==========================================================================

import * as functions from "firebase-functions/v2";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";
import {HttpsError, CallableRequest} from "firebase-functions/v2/https";
import {Timestamp} from "firebase-admin/firestore";
import {onObjectFinalized} from "firebase-functions/v2/storage";

// --- Imports de Node.js ---
import * as os from "os";
import * as path from "path";
import * as fs from "fs";

// --- Inicialización de Firebase Admin SDK ---
try {
  if (admin.apps.length === 0) {
    admin.initializeApp();
    logger.log("Firebase Admin SDK inicializado.");
  }
} catch (e) {
  logger.error("Error inicializando Firebase Admin SDK:", e);
}
const db = admin.firestore();
const storage = admin.storage();

// --- Interfaces TypeScript ---
interface RespuestaUsuarioData {
  preguntaId: string;
  respuestaId: string;
}
interface SubmitTestData {
  testId?: string;
  respuestas?: RespuestaUsuarioData[];
}
interface EmotionalAnalysisResponseData {
  disgusted?: number; angry?: number; happy?: number; scared?: number;
  neutral?: number; surprised?: number; sad?: number;
  [key: string]: number | undefined;
}
interface OrientationRequestData {
  nombre?: string;
  emociones?: { [key: string]: number };
}


// ==========================================================================
// 1. Cloud Function: submitTestResults (HTTPS Callable)
// ==========================================================================
export const submitTestResults = functions.https.onCall(
    async (request: CallableRequest<SubmitTestData>) => {
      logger.info("submitTestResults: Ejecución iniciada.", {structuredData: true});
      const functionStartTime = Date.now();

      // 1. Validar Autenticación
      if (!request.auth) {
        logger.error("submitTestResults: Error - No autenticado.");
        throw new HttpsError("unauthenticated", "Autenticación requerida.");
      }
      const userId = request.auth.uid;
      logger.log("submitTestResults: Usuario autenticado:", userId);

      // 2. Validar y Extraer Datos de Entrada
      const data = request.data;
      logger.log("submitTestResults: Datos recibidos:", data);
      const testId = data.testId;
      const respuestasUsuario = data.respuestas;

      if (!testId || typeof testId !== "string" || testId.trim() === "") {
        logger.error("submitTestResults: testId inválido.", {testId});
        throw new HttpsError("invalid-argument", "Parámetro 'testId' inválido.");
      }
      if (!Array.isArray(respuestasUsuario) || respuestasUsuario.length === 0) {
        logger.error("submitTestResults: 'respuestas' inválido.",
            {respuestas: respuestasUsuario});
        throw new HttpsError("invalid-argument",
            "Parámetro 'respuestas' inválido o vacío.");
      }
      for (const resp of respuestasUsuario) {
        if (!resp || typeof resp.preguntaId !== "string" ||
            resp.preguntaId.trim() === "" ||
            typeof resp.respuestaId !== "string" ||
            resp.respuestaId.trim() === "") {
          logger.error("submitTestResults: Formato respuesta inválido.",
              {respuesta: resp});
          throw new HttpsError("invalid-argument",
              "Formato de respuesta de usuario inválido.");
        }
      }
      logger.info(`submitTestResults: Datos validados - testId=${testId}, ` +
                  `respuestas=${respuestasUsuario.length}`);

      try {
        // 3. Calcular Puntaje Total
        logger.info("submitTestResults: Calculando puntaje...");
        let puntajeTotal = 0;
        const respuestaIds = respuestasUsuario.map((r) => r.respuestaId);
        const respuestasDocsRefs = respuestaIds.map((id) =>
          db.collection("respuestas").doc(id));
        const respuestasSnapshots = await db.getAll(...respuestasDocsRefs);
        logger.info(`submitTestResults: ${respuestasSnapshots.length}/` +
                    `${respuestaIds.length} docs respuestas obtenidos.`);

        if (respuestasSnapshots.length !== respuestaIds.length) {
          const foundIds = respuestasSnapshots.map((s) => s.id);
          const missingIds = respuestaIds.filter((id) => !foundIds.includes(id));
          logger.error("submitTestResults: No se encontraron docs respuesta. " +
                       `IDs faltantes: ${missingIds.join(", ")}`);
          throw new HttpsError("not-found",
              "Una o más respuestas seleccionadas no son válidas.");
        }

        for (const docSnap of respuestasSnapshots) {
          const respuestaData = docSnap.data();
          if (!respuestaData) {
            throw new HttpsError("internal",
                `Sin datos en Respuesta ${docSnap.id}.`);
          }
          const puntajeRespuesta = respuestaData.numeroRespuesta;
          if (typeof puntajeRespuesta !== "number") {
            throw new HttpsError("internal",
                `Sin puntaje numérico en Respuesta ${docSnap.id}.`);
          }
          puntajeTotal += puntajeRespuesta;
        }
        logger.info(`submitTestResults: Puntaje total: ${puntajeTotal}`);

        // 4. Determinar Diagnóstico
        logger.info("submitTestResults: Buscando puntuación " +
                    `para testId=${testId}, puntaje=${puntajeTotal}`);
        const puntuacionQuery = db.collection("puntuaciones")
            .where("testId", "==", testId)
            .where("rangoInferior", "<=", puntajeTotal);
        const puntuacionSnapshot = await puntuacionQuery.get();
        let diagnosticoTexto = "Diagnóstico no disponible";
        let puntuacionDocId: string | null = null;

        if (!puntuacionSnapshot.empty) {
          const matchingPuntuaciones = puntuacionSnapshot.docs.filter((doc) => {
            const d = doc.data();
            return typeof d.rangoSuperior === "number" &&
                   puntajeTotal <= d.rangoSuperior;
          });
          if (matchingPuntuaciones.length === 1) {
            diagnosticoTexto = matchingPuntuaciones[0].data().diagnostico as string || "?";
            puntuacionDocId = matchingPuntuaciones[0].id;
          } else if (matchingPuntuaciones.length > 1) {
            logger.warn("Múltiples rangos coinciden...");
            diagnosticoTexto = matchingPuntuaciones[0].data().diagnostico as string || "?";
            puntuacionDocId = matchingPuntuaciones[0].id;
          } else { logger.warn("Puntaje fuera rango superior."); }
        } else { logger.warn("No se encontró rango inicial."); }
        logger.info(`Diagnóstico: '${diagnosticoTexto}', ` +
                    `Puntuacion ID: ${puntuacionDocId}`);

        // 5. Guardar Resultados en Transacción
        logger.info("submitTestResults: Iniciando transacción...");
        const newDiagnosticoId = await db.runTransaction(async (transaction) => {
          const timestampActual = Timestamp.now();
          const diagnosticoRef = db.collection("diagnosticos").doc();
          transaction.set(diagnosticoRef, {
            personaId: userId, testId: testId, fecha: timestampActual,
            puntaje: puntajeTotal, puntuacionId: puntuacionDocId,
          });

          respuestasUsuario.forEach((respuestaUser) => {
            const respUsuarioRef = db.collection("respuestasUsuario").doc();
            transaction.set(respUsuarioRef, {
              diagnosticoId: diagnosticoRef.id,
              personaId: userId,
              testId: testId,
              preguntaId: respuestaUser.preguntaId,
              respuestaId: respuestaUser.respuestaId,
            });
          });
          logger.info(`  Transacción preparada para ${diagnosticoRef.id} ` +
                      `y ${respuestasUsuario.length} resp.`);
          return diagnosticoRef.id;
        });
        logger.info(`submitTestResults: Transacción completada. `+
                    `Diagnostico ID: ${newDiagnosticoId}`);

        // 6. Devolver Resultado Exitoso
        const executionTime = Date.now() - functionStartTime;
        logger.info(`submitTestResults: Finalizada OK en ${executionTime} ms.`);
        return {
          success: true,
          diagnosticoId: newDiagnosticoId,
          diagnostico: diagnosticoTexto,
          puntaje: puntajeTotal,
        };
      } catch (error: any) {
        logger.error("submitTestResults: Error en ejecución:", error);
        if (error instanceof HttpsError) {
          throw error;
        }
        throw new HttpsError("internal",
            error.message || "Error interno al procesar el test.");
      }
    });

// ==========================================================================
// 2. Cloud Function: generarRespuestaEmocional (HTTPS Callable)
// ==========================================================================
export const generarRespuestaEmocional = functions.https.onCall(
    async (request: CallableRequest<OrientationRequestData>) => {
      logger.info("generarRespuestaEmocional: Iniciando.", {structuredData: true});
      const data = request.data;
      logger.log("generarRespuestaEmocional: Datos recibidos:", data);
      const nombre = data.nombre;
      const emociones = data.emociones;

      if (!nombre || typeof nombre !== "string" || nombre.trim() === "") {
        throw new HttpsError("invalid-argument", "'nombre' inválido.");
      }
      if (!emociones || typeof emociones !== "object" ||
          Object.keys(emociones).length === 0) {
        throw new HttpsError("invalid-argument", "'emociones' inválido.");
      }

      try {
        logger.info(`Generando respuesta para ${nombre}...`);

        // --- Lógica de Generación de Respuesta (EJEMPLO) ---
        const emocionDominante = Object.entries(emociones)
            .reduce((max, entry) => entry[1] > max[1] ? entry : max,
                ["desconocida", -1])[0];

        let mensajesRespuesta: string[] = [];
        switch (emocionDominante.toLowerCase()) {
          case "enojado": mensajesRespuesta = [`Hola ${nombre}. Parece que sientes enojo. Tomar una pausa puede ayudar.`, "¿Qué situación generó ese sentimiento?", "Recuerda que controlar la reacción está en tus manos."]; break;
          case "triste": mensajesRespuesta = [`${nombre}, entiendo que te sientas así. No estás solo/a.`, "Permitirte sentir es importante.", "Considera hablar con alguien cercano o un profesional."]; break;
          case "miedo": mensajesRespuesta = [`Sentir miedo es una reacción natural, ${nombre}.`, "Identificar la fuente del miedo es el primer paso.", "Busca técnicas de relajación que te funcionen."]; break;
          case "feliz": mensajesRespuesta = [`¡Me alegra verte feliz, ${nombre}!`, "Aprovecha esta energía positiva.", "¿Qué te hizo sentir así hoy?"]; break;
          case "disgustado": mensajesRespuesta = [`${nombre}, parece que algo te causa disgusto.`, "¿Puedes identificar qué es?", "A veces alejarse de la situación ayuda."]; break;
          case "sorpresa": mensajesRespuesta = [`Vaya, ${nombre}, parece que algo te sorprendió.`, "¿Fue una sorpresa agradable o desagradable?", "Tómate un momento para procesarlo."]; break;
          case "neutral": mensajesRespuesta = [`Hola ${nombre}. Te noto en calma.`, "Es un buen momento para reflexionar o simplemente estar presente.", "¿Hay algo en lo que pueda ayudarte?"]; break;
          default: mensajesRespuesta = [`Hola ${nombre}. Estoy aquí para escucharte.`, "¿Cómo te sientes hoy?"];
        }
        // --- FIN Lógica de Generación ---

        logger.info("Respuesta generada.");
        return {success: true, message: "Respuesta generada.", response: mensajesRespuesta};
      } catch (error: any) {
        logger.error("generarRespuestaEmocional: Error:", error);
        if (error instanceof HttpsError) throw error;
        throw new HttpsError("internal",
            error.message || "Error al generar la respuesta.");
      }
    });


// ==========================================================================
// 3. Cloud Function: processUploadedVideo (Storage Triggered)
// ==========================================================================
export const processUploadedVideo = onObjectFinalized(
    {
      timeoutSeconds: 300,
      memory: "1GiB",
      // cpu: 1 // Opcional: especificar CPU si es necesario
    },
    async (event): Promise<void> => {
      const object = event.data;
      const filePath = object.name;
      const contentType = object.contentType;
      const bucketName = object.bucket;

      logger.info(`Archivo detectado: ${filePath}`, {structuredData: true});

      // 1. Validaciones iniciales
      if (!filePath || !filePath.startsWith("videos/")) {
        logger.log("Ignorado: No está en 'videos/'", {path: filePath}); return;
      }
      if (!contentType || !contentType.startsWith("video/")) {
        logger.log("Ignorado: No es video", {type: contentType}); return;
      }

      const parts = filePath.split("/");
      if (parts.length !== 3) {
        logger.warn("Ruta inesperada:", {filePath}); return;
      }
      const userId = parts[1];
      const videoFileName = parts[2];
      const videoId = path.parse(videoFileName).name;

      if (!userId || !videoId) {
        logger.error("No se pudo extraer userId/videoId", {filePath}); return;
      }

      logger.info(`Procesando: videoId=${videoId}, userId=${userId}`);
      const resultsDocRef = db.collection("analisisResultados").doc(videoId);
      let tempFilePath: string | null = null;

      try {
        // 2. Actualizar estado a "procesando"
        logger.log("Actualizando estado a 'procesando'", {videoId});
        await resultsDocRef.update({
          status: "procesando",
          error: null, // Limpiar error previo
          timestamp: Timestamp.now(), // Actualizar timestamp
        });

        // 3. Descargar video
        const bucket = storage.bucket(bucketName);
        tempFilePath = path.join(os.tmpdir(), videoFileName);
        logger.log(`Descargando gs://${bucketName}/${filePath} a ${tempFilePath}...`);
        await bucket.file(filePath).download({destination: tempFilePath});
        logger.log("Video descargado.");

        // 4. --- IMPLEMENTAR LÓGICA DE ANÁLISIS REAL ---
        logger.warn("==> USANDO DATOS DE EMOCIONES FALSOS <==", {videoId});
        await new Promise((resolve) => setTimeout(resolve, 7000)); // Simulación
        const emotionResults: EmotionalAnalysisResponseData = {
          disgusted: Math.random() * 0.1, angry: Math.random() * 0.2,
          happy: Math.random() * 0.5, scared: Math.random() * 0.3,
          neutral: Math.random() * 0.4, surprised: Math.random() * 0.1,
          sad: Math.random() * 0.2,
        };
        logger.info("Análisis (falso) completado.", {videoId});
        // ------------------------------------------

        // 5. Actualizar Firestore a "completado"
        await resultsDocRef.update({
          status: "completado",
          resultados: emotionResults,
          error: null,
          timestamp: Timestamp.now(),
        });
        logger.log("Documento actualizado a 'completado'.", {videoId});
      } catch (error: any) {
        // 6. Manejar errores
        logger.error(`Error procesando ${filePath}:`, error);
        try {
          await resultsDocRef.update({
            status: "error",
            error: error.message || "Error desconocido en procesamiento.",
            timestamp: Timestamp.now(),
          });
          logger.log("Documento actualizado a 'error'.", {videoId});
        } catch (dbError) {
          logger.error("Falló al actualizar estado de error en Firestore:",
              {videoId, dbError});
        }
      } finally {
        // 7. Limpiar archivo temporal
        if (tempFilePath && fs.existsSync(tempFilePath)) {
          try {
            fs.unlinkSync(tempFilePath);
            logger.log(`Archivo temporal eliminado: ${tempFilePath}`);
          } catch (unlinkError) {
            logger.error("Error eliminando archivo temporal:",
                {path: tempFilePath, unlinkError});
          }
        }
      }
    }); 


  


// ==========================================================================  
// 4. Cloud Function: createUserProfile (HTTPS Callable)  
// ==========================================================================  
export const createUserProfile = functions.https.onCall(  
    async (request: CallableRequest<UserRegistrationData>) => {  
      logger.info("createUserProfile: Ejecución iniciada.", {structuredData: true});  
      const functionStartTime = Date.now();  
  
      // 1. Validar Datos de Entrada  
      const data = request.data;  
      logger.log("createUserProfile: Datos recibidos:", data);  
  
      // Validaciones básicas  
      if (!data.email || !data.password || !data.firstName || !data.lastName ||   
          !data.documentType || !data.documentNumber || !data.gender || !data.birthDate) {  
        logger.error("createUserProfile: Datos obligatorios faltantes.");  
        throw new HttpsError("invalid-argument", "Faltan datos obligatorios para el registro.");  
      }  
  
      // Validar formato de email  
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;  
      if (!emailRegex.test(data.email)) {  
        throw new HttpsError("invalid-argument", "Formato de email inválido.");  
      }  
  
      // Validar mayoría de edad  
      const birthDate = new Date(data.birthDate);  
      const today = new Date();  
      const age = today.getFullYear() - birthDate.getFullYear();  
      if (age < 18) {  
        throw new HttpsError("invalid-argument", "Debe ser mayor de 18 años para registrarse.");  
      }  
  
      try {  
        // 2. Crear usuario en Firebase Authentication  
        logger.info("createUserProfile: Creando usuario en Firebase Auth...");  
        const userRecord = await admin.auth().createUser({  
          email: data.email,  
          password: data.password,  
          displayName: `${data.firstName} ${data.lastName}`,  
        });  
          
        const userId = userRecord.uid;  
        logger.info(`createUserProfile: Usuario creado en Auth: ${userId}`);  
  
        // 3. Buscar tipousuarioid para "Paciente"  
        const tipoUsuarioQuery = await db.collection("tiposUsuario")  
            .where("descripcion", "==", "Paciente")  
            .limit(1)  
            .get();  
          
        let tipousuarioid = null;  
        if (!tipoUsuarioQuery.empty) {  
          tipousuarioid = tipoUsuarioQuery.docs[0].id;  
        } else {  
          logger.warn("createUserProfile: No se encontró tipo de usuario 'Paciente'");  
        }  
  
        // 4. Buscar IDs de referencia (género, tipo documento, ubicación)  
        const [genderDoc, docTypeDoc, ubicacionDoc] = await Promise.all([  
          db.collection("generos").where("descripcion", "==", data.gender).limit(1).get(),  
          db.collection("tiposDocumento").where("descripcion", "==", data.documentType).limit(1).get(),  
          data.ubicacion ? db.collection("ubicaciones").where("descripcion", "==", data.ubicacion).limit(1).get() : null  
        ]);  
  
        const generoid = !genderDoc.empty ? genderDoc.docs[0].id : null;  
        const tipodocumentoid = !docTypeDoc.empty ? docTypeDoc.docs[0].id : null;  
        const ubicacionid = ubicacionDoc && !ubicacionDoc.empty ? ubicacionDoc.docs[0].id : null;  
  
        // 5. Guardar datos en Firestore usando transacción  
        logger.info("createUserProfile: Iniciando transacción para guardar datos...");  
        await db.runTransaction(async (transaction) => {  
          const timestampActual = Timestamp.now();  
            
          // Crear documento en colección 'personas'  
          const personaRef = db.collection("personas").doc(userId);  
          transaction.set(personaRef, {  
            nombres: data.firstName,  
            apellidoPaterno: data.lastName,  
            apellidoMaterno: data.middleName || "",  
            fechaNacimiento: data.birthDate,  
            telefono: data.phone || "",  
            correo: data.email,  
            numeroDocumento: data.documentNumber,  
            generoid: generoid,  
            tipodocumentoid: tipodocumentoid,  
            ubicacionid: ubicacionid,  
            fechaCreacion: timestampActual,  
            activo: true  
          });  
  
          // Crear documento en colección 'usuarios'  
          const usuarioRef = db.collection("usuarios").doc(userId);  
          transaction.set(usuarioRef, {  
            personaId: userId,  
            tipousuarioid: tipousuarioid,  
            fechaCreacion: timestampActual,  
            emailVerificado: false,  
            activo: true  
          });  
  
          logger.info(`createUserProfile: Transacción preparada para usuario ${userId}`);  
        });  
  
        // 6. Enviar email de verificación  
        logger.info("createUserProfile: Enviando email de verificación...");  
        await admin.auth().generateEmailVerificationLink(data.email);  
  
        // 7. Devolver resultado exitoso  
        const executionTime = Date.now() - functionStartTime;  
        logger.info(`createUserProfile: Finalizada OK en ${executionTime} ms.`);  
          
        return {  
          success: true,  
          userId: userId,  
          message: "Usuario registrado exitosamente. Revisa tu correo para verificar tu cuenta.",  
        };  
  
      } catch (error: any) {  
        logger.error("createUserProfile: Error en ejecución:", error);  
          
        // Si el error es de Firebase Auth, manejarlo específicamente  
        if (error.code === 'auth/email-already-exists') {  
          throw new HttpsError("already-exists", "Ya existe una cuenta con este correo electrónico.");  
        } else if (error.code === 'auth/weak-password') {  
          throw new HttpsError("invalid-argument", "La contraseña es muy débil.");  
        }  
          
        if (error instanceof HttpsError) {  
          throw error;  
        }  
        throw new HttpsError("internal", error.message || "Error interno al crear el perfil de usuario.");  
      }  
    });

