El objetivo de este proyecto es crear una aplicación con Java que permita jugar al juego de las tres en raya, pero de una temática concreta. En nuestro caso, todo irá relacionado con el tema del futbol, pero se puede enfocar en cualquier otro contenido que se desee.

1.	BASE DE DATOS 
En esta aplicación trabajaremos con bases de datos no relacionales, concretamente utilizando MongoDB. Las bases de datos no relacionales, también conocidas como NoSQL, son sistemas de gestión de datos diseñados para manejar grandes volúmenes de datos de manera flexible y escalable, sin adherirse a un modelo de datos tabular como en las bases de datos relacionales. 
Estas bases de datos permiten almacenar y recuperar datos de forma eficiente mediante estructuras como documentos, columnas o grafos, lo que las hace ideales para aplicaciones con requisitos de almacenamiento y consultas muy variados, como aplicaciones web, IoT o Big Data. 
MongoDB es una base de datos no relacional de código abierto, orientada a documentos, diseñada para almacenar y recuperar datos de manera eficiente en entornos escalables y distribuidos. Utiliza un modelo de datos flexible basado en documentos JSON, lo que permite almacenar datos de forma similar a la estructura de objetos utilizada en muchos lenguajes de programación. Es altamente escalable y puede distribuirse en clústeres para manejar grandes volúmenes de datos y cargas de trabajo intensivas. 
Además, ofrece una amplia gama de funcionalidades, incluyendo consultas complejas, indexación flexible, agregaciones poderosas y replicación automática para garantizar la disponibilidad y la resiliencia de los datos. 
En la base de datos tendremos una colección Jugador donde, para cada documento, almacenaremos un jugador distinto. En este caso, tan sólo nos vamos a centrar en jugadores de la Liga Española de Futbol o LaLiga. La información se puede recopilar mediante la web de Transfermarkt.es que es una web donde se acumula además de información, diferentes estadísticas que pueden ser valiosas para almacenar y darle aún más complejidad al juego. 
La información que como mínimo se debe almacenar de cada Jugador es la siguiente: 
− Nombre del jugador (Un solo valor en forma de String) 
Ejemplo: Cristiano Ronaldo 
− Nacionalidad del jugador (Un solo valor en forma de String) 
Ejemplo: Portugal
− Edad del jugador (Un solo valor en forma de Integer) 
Ejemplo: 40 
− Posiciones o demarcación que puede ocupar (Una lista de valores String) 
Ejemplo: [ DC, EI, ED ] 
− Equipos en los que ha estado el jugador (Una lista de valores String) 
Ejemplo: [ Sporting Lisboa, Manchester United, Real Madrid, Juventus, Al-Nassr ] (…) 

Esta información se puede sacar de la web de Transfermarkt.es que es una página web donde guarda toda la información referente al mundo del futbol, como puntuaciones, resultados, noticias sobre transferencias y calendarios y palmarés de los equipos de las ligas europeas. 

Concretamente si se quiere obtener la información referente a los equipos de la liga española. 

En él se puede ver información tabulada sobre los clubs de la liga a nivel general, aunque esta información global, no nos interesa, nos interesan particularmente los jugadores de dichos equipos. 
Para llegar a ellos, se hace pinchando en el equipo concretamente, por ejemplo, si queremos llegar hasta los jugadores del Real Madrid, pincharíamos en su nombre y nos llevaría a su información particular: 

Por último, como lo que queremos es la información de los jugadores, en la parte inferior tenemos una tabla con esa lista de jugadores del equipo. Tendremos que ir uno a uno recopilando la información requerida para introducirla en nuestra base de datos. 

La mayoría de la información está en la pestaña principal de “PERFIL” salvo la parte de la lista de equipos que habría que irse a la pestaña de “FICHAJES”. 

2.	OBJETIVO 
El tres en raya o en otros países llamado tres en línea, es un juego que comúnmente se juega con lápiz y papel entre dos jugadores. Un jugador suele marcar con una “X” y otro con un “O”, aunque hay muchas variantes. Dichas marcas se realizan en los espacios vacíos de un tablero de 3x3 de manera alterna, es decir, primero es el turno de uno y posteriormente del otro, hasta que se rellenan todos los huecos o se obtiene el ganador.

 El juego tiene ganador si se consigue realizar una línea recta de símbolos. 

3.	FUNCIONAMIENTO 
En nuestro caso particular, no es tan sencillo como marcar la casilla en cada turno, en la parte superior y la parte izquierda habrá unas categorías y para que el usuario pueda marcar esa casilla con su símbolo, tendrá que acertar y cumplir ambas categorías que están marcadas.  Es decir, si el usuario quiere marcar la posición (1,1), tendría que decir un jugador que sea POR y que haya estado o pertenezca al VALENCIA. En ese caso, el jugador pintará su marca. Las tres categorías superiores van ligadas al igual que las laterales, es decir, si en la parte superior de manera aleatoria sale un equipo, los tres serán del mismo tipo, tal y como se puede ver en el ejemplo superior. En ese caso cruzan dos categorías: equipo VS posiciones. En ese caso las categorías verticales son los tres equipos y las horizontales son las tres posiciones. 
Esas categorías podrán ser: 
− Un Equipo (El jugador tiene que pertenecer o haber pertenecido a ese equipo) 
− Un País (El jugador tiene que tener esa nacionalidad) 
− Una Posición (El jugador tiene que jugar en esa posición del campo) 
− Una Edad (El jugador tiene que tener una edad mayor/menor que la estipulada) 

Todos los huecos del tablero van a ser botones de modo que, al presionarlos, se compruebe un campo de texto o mejor dicho un “TextField” donde se cogerá ese texto que el usuario haya metido y se comprobará a través de una consulta si el texto introducido, coincide con alguno de los valores que una consulta a la base de datos nos devuelve. 

En resumen, el usuario introducirá un nombre en el campo de texto y al pulsar uno de los botones del tablero, se realice una consulta donde coja la categoría superior y lateral y si el nombre está en el resultado, pondrá su marca. 
Una vez que ponga su marca, se deberá comprobar si hay alguna combinación de marcas seguidas que consigan obtener ganador, si no es así, se seguirá con el juego, pero ahora cambiaremos de turno y le tocará al siguiente jugador. 

-	Ejemplo:
El usuario quiere introducir el nombre de la casilla (2,2), tal y como se marca en el dibujo En ese caso introduciría el nombre en el campo de texto: “Miguel Gutiérrez” y el programa hará una consulta a la base de datos. 

Se tendrá que coger la categoría superior: GIRONA, darse cuenta que es un equipo y coger la categoría lateral: LI y darse cuenta que es una posición. Para ello se tendrá que controlar los posibles valores aleatorios que pueden salir en esas categorías previamente… 

Una vez que sabemos ambas categorías hay que hacer un filtro y realizar una consulta a la base de datos y sacar: “Todos los nombres de los jugadores que tengan en su lista de equipos al GIRONA y que en su lista de posiciones también tengan LI…” ese resultado de la consulta se devolverá en forma de lista de nombres y si el texto que ha introducido el usuario es exactamente uno de ellos, pintará y cambiará el fondo del botón con la imagen o marca del usuario que esté jugando en ese momento.

Como recomendación, sería recomendable que al usuario le ofrezcas ayuda y que de alguna forma se pueda autocompletar ese nombre en función con los nombres que tiene guardados en la base de datos, pero eso es un valor añadido, no es un requisito obligatorio. 
Se debe tener un sitio reservado en la interfaz para marcar si es un acierto o un fallo, ya que en caso de acertar cambiará el fondo del botón y se inhabilitará, pero en caso de error… se debe marcar de alguna manera. 

Acierte o falle el campo de texto tiene que borrarse para dejarlo limpio para el siguiente intento. 

También habrá que reservar una parte de la interfaz para marcar si se ha ganado el juego y en caso de que así sea, poder reiniciar una nueva partida, para ello tendría que aparecer en ese caso y solo en ese caso un botón de reinicio. Cuando este sea pulsado, se tendrán que reiniciar y elegir nuevas categorías y limpiar el tablero de marcas. 

Es recomendable que también en una parte se establezca una tabla de puntuaciones donde tengamos dos registros numéricos, donde se vaya incrementando las partidas ganadas de los competidores actuales. 

4.	INTERFAZ DE USUARIO
En este caso, el diseño de la interfaz será libre, pero tendrá que cumplir con la funcionalidad establecida. Un ejemplo de ello es el diseño que se propone en la parte superior. 

Estos dos últimos son dos ejemplos de aplicaciones web reales con un funcionamiento similar. 

Estas dos soluciones son más profesionales porque almacenan imágenes en sus bases de datos, en nuestro caso no es un requisito obligatorio, pero si es opcional y recomendable, de hecho, es un aspecto a valorar en la práctica y como tal, tendrá un peso en la calificación de la práctica.
