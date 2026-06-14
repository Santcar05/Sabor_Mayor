/**
 * Producción. El frontend habla SIEMPRE con el API Gateway de Spring Cloud,
 * nunca con los puertos individuales de los microservicios.
 */
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080',
  wsUrl: 'http://localhost:8080/ws',
};
