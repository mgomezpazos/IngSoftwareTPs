# Gift Card Platform – Frontend + Backend

Este proyecto implementa la plataforma de Gift Cards solicitada, con backend desarrollado bajo TDD y un frontend web extra que hicimos porque nos pareció divertido el TP y nos intrigaba cómo se vería todo de manera interactiva!

## Cómo correr el proyecto

1) Levantar los backends

- Ejecutar la aplicación de MerchantApplication

- Ejecutar la aplicación de GiftCardApplication

(Asegurarse de que ambos estén corriendo antes de abrir el frontend)

2) Abrir el frontend

- Localizar el archivo .html del frontend

- Abrirlo en el navegador Google Chrome

3) Crear un nuevo usuario

- Tu usuario no está precargado, así que vas a tener que crear uno nuevo.

- Después de crearlo, logueate con ese mismo usuario y contraseña.

4) Iniciar sesión

- El sistema genera un token válido por 5 minutos.

- Con el token activo vas a poder:

  - Reclamar tus Gift Cards

  - Consultar saldo

  - Ver el detalle de gastos

5) Probarlo libremente! 

- Una vez logueado, podés “comprar lo que quieras” como si fueras un cliente más.

- Los merchants registrados van a impactar automáticamente en el balance de las tarjetas.

## Ejemplo de flujo de uso

Para que quede más claro cómo probarlo, un ejemplo de interacción sería:

1) Crear un usuario nuevo:

- Usuario: Emilio

- Password: 0codigoRepetido!

2) Loguearse con ese usuario y contraseña → se obtiene un token válido por 5 minutos.

3) Reclamar la(s) Gift Card(s) disponibles.

4) Consultar saldo inicial (ejemplo: $5000).

5) Simular una compra desde un merchant registrado (ejemplo: comprar en Harley Davidson por $1200).

6) Volver a consultar saldo y ver el detalle de gastos actualizado:

- Saldo actual: $3800

- Detalle: “Compra en Harley Davidson por $1200”

7) Seguir probando hasta que expire el token (pasados 5 minutos) → el sistema pedirá volver a loguearse.

## Notas técnicas

El backend se desarrolló siguiendo la técnica de TDD, con cobertura completa de tests y cuidando buenas prácticas de diseño:

- Nada de código repetido

- Evitar ifs innecesarios

- Responsabilidades bien distribuidas

- Nombres claros y expresivos

El frontend es un plus que quisimos hacer por iniciativa propia: nos parecía un lindo desafío extra y la manera más divertida de probar lo que íbamos construyendo :)

## Autoras

Manuela y Zöe
