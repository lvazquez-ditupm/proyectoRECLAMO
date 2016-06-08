# Proyecto RECLAMO
## Contenido
* RECLAMO: Contiene el sistema RECLAMO como tal:
    * RECLAMO.jar: Ejecutable del sistema
	* GeneradorAlertas.jar: Envía alertas al puerto 512 (UDP) de la IP establecida
	* communication-debug: Ejecuta las respuestas en el sistema 
	* build/: Código del sistema RECLAMO
	* config_files/: Ficheros de configuración del sistema RECLAMO
	    * airsResponseExecutor.conf: Configuración del módulo de respuestas (se pueden variar las duraciones de las respuestas)
	    * evaluationSystemExecutor.conf: Configuración del sistema de evaluación de eficacia de las respuestas
	    * networkContextAnomalyDetector.conf: Configuración del módulo de contexto de red
	    * parser.conf: Configuración del parser
	    * reclamo.conf: Configuración de RECLAMO (se pueden variar distintos umbrales para la toma de decisiones)
	    * systemContextAnomalyDetector.conf: Configuración del módulo de contexto de sistema
	* lib/: Librerías compiladas. Deberán modificarse los siguientes ficheros para ajustarlos a cada sistema:
	    * idmefParser: será necesario hacer una correspondencia entre los mensajes de alerta IDMEF y el tipo de ataque que corresponde (ej: "Se ha detectado un ataque de Denegación de Servicio" -> "DoS").
	    * snortParser: ídem, para el IDS Snort.
* ext_config_files: Contiene los distintos ficheros de configuración necesarios para el funcionamiento correcto del software externo a RECLAMO (Nagios, SANCP...). Su uso se explica en la Wiki
* libraries: Contiene el código de las librerías que se usan en RECLAMO
* ontologies: Contiene todos los ficheros .owl para el funcionamiento del sistema
* running_files: Contiene los scripts de arranque del escenario y otros necesarios para pruebas
    * autogen: Contiene un script que se encarga de generar el fichero hosts.cfg de Nagios automáticamente a partir de los activos registrados en la base de datos
* vnx_scenarios: Contiene los ficheros necesarios para virtualizar la red del escenario con VNX

## Consideraciones
* Para que el módulo de respuestas funcione, debe copiarse el fichero ext_config_files/agent.conf a /etc)
* Los ficheros situados en RECLAMO/config_files deben modificarse para que las URIs de las ontologías apunten a la ruta en la que se ejecuta el código (además de otros parámetros necesarios para poder ejecutarse en cualquier sistema, en los propios ficheros se explica para qué sirve cada uno).
* Actualmente, el número de ataques y respuestas funcionales está limitado a los que aparecen en este [link](http://reclamoairsupm.wikidot.com/start#toc21).

## Instalación
### Instalación del sistema
Toda la información se puede encontrar en el siguiente link:
http://reclamoairsupm.wikidot.com/

### Ejecución de RECLAMO y Generador de Alertas
Una vez se haya preparado el sistema correctamente (apartado anterior), y se hayan modificado los ficheros de configuración, se podrá ejecutar el sistema con el comando:
```sh
$ sudo java -jar RECLAMO.jar [Puerto_UDP] [Puerto_TCP] [IP_local] [Máscara de red]
# Ejemplo: 
$ sudo java -jar RECLAMO.jar 512 6868 10.0.1.1 255.255.255.0
```
Para enviar alertas al puerto 512 del sistema de forma manual, se puede usar el siguiente comando:
```sh
$ java -jar GeneradorAlertas.jar [IP_destino] ['Mensaje']
# Ejemplo
$ java -jar GeneradorAlertas 10.0.1.1 'Alerta de prueba: "ALERTA"'
```
