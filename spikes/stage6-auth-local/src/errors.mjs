import {randomUUID} from 'node:crypto';
export class ApiError extends Error {
 constructor(status,code,message){super(message);this.status=status;this.code=code;}
}
export const unauthenticated=()=>new ApiError(401,'unauthenticated','Authentication required');
export const forbidden=()=>new ApiError(403,'forbidden','Operation not permitted');
export const notFound=()=>new ApiError(404,'not_found','Resource not found');
export const invalid=()=>new ApiError(400,'validation_failed','Invalid request');
export const unavailable=()=>new ApiError(503,'service_unavailable','Service unavailable');
export function failure(error,requestId=randomUUID()) {
 const safe=error instanceof ApiError?error:unavailable();
 return {status:safe.status,body:{requestId,error:{code:safe.code,message:safe.message,requestId}}};
}
export function allowObject(value,keys){
 if(!value || typeof value!=='object' || Array.isArray(value) || Object.getPrototypeOf(value)!==Object.prototype ||
   Object.keys(value).some(k=>!keys.includes(k))) throw invalid();
}
export function uuid(value){if(typeof value!=='string'||!/^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value))throw invalid();return value.toLowerCase();}
export function readClock(clock){
 try{const value=clock();if(!Number.isSafeInteger(value)||!Number.isFinite(new Date(value).getTime()))throw unavailable();return value;}
 catch{throw unavailable();}
}
