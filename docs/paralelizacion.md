Mover una partícula solo requiere conocer su posición, su velocidad y el tiempo del paso. Por ejemplo, para mover la última del arreglo podemos hacer la cuenta aunque todavía se esté calculando la primera. Esto nos permitió repartir el sistema y después reunir las respuestas.

## 1. Cómo repartimos el arreglo

Empezamos con los índices `0` y `s.length`. Si el tramo tiene más elementos que el umbral, buscamos el índice del medio. `parallel` permite calcular la parte izquierda y la derecha al mismo tiempo. Cuando la longitud ya cabe en el límite, se resuelve ese tramo directamente.

Las tres funciones siguen esa idea. Lo que cambia es el resultado que devuelve cada parte.

| Función | Qué calcula cada tramo | Cómo se juntan los dos lados |
|---|---|---|
| `pasoParalelo` | Un arreglo con las partículas movidas | `izquierda ++ derecha` |
| `energiaParalela` | La energía de sus partículas | Se suman las dos energías |
| `centroDeMasaParalelo` | La suma de posiciones por masa y la masa del tramo | Se suman por separado los dos datos |

En el movimiento importa el orden. Si la parte derecha termina primero, igual se coloca después de la izquierda. Para el centro de masa se espera hasta el final para dividir. Así una mitad con más masa tiene el peso que le corresponde.

## 2. Cómo se tomaron los tiempos

La medición se hizo en un Intel Core i7-14650HX con 16 núcleos físicos. Java mostró 24 procesadores lógicos disponibles. Usamos dos arreglos, uno de 500 000 partículas y otro de 1 000 000, con `dt = 0.5`.

Los tiempos de la tabla salieron de ejecutar `.\gradlew.bat run` por separado en la rama `juanq`. El arreglo de entrada se creó antes de empezar a medir. Así, el tiempo de preparar las partículas quedó fuera de la comparación.

Para cada tamaño se midió `pasoSecuencial` y luego `pasoParalelo` con cuatro umbrales. Se usó `medir(5)` con el calentamiento de ScalaMeter. Cada tiempo de la tabla es el valor central de cinco muestras. La aceleración se calculó antes de redondear los números.

| Partículas | Umbral | Secuencial (ms) | Paralelo (ms) | Aceleración |
|---:|---:|---:|---:|---:|
| 500 000 | 500 000 | 5.510 | 5.600 | 0.984 |
| 500 000 | 250 000 | 5.510 | 4.275 | 1.289 |
| 500 000 | 62 500 | 5.510 | 4.350 | 1.267 |
| 500 000 | 15 625 | 5.510 | 4.260 | 1.293 |
| 1 000 000 | 1 000 000 | 15.655 | 13.981 | 1.120 |
| 1 000 000 | 500 000 | 15.655 | 9.230 | 1.696 |
| 1 000 000 | 125 000 | 15.655 | 11.310 | 1.384 |
| 1 000 000 | 31 250 | 15.655 | 11.656 | 1.343 |

### Qué pasó con el control

La primera fila de cada tamaño usa un umbral igual a la cantidad de partículas. En ese caso, `pasoSecuencial` y `pasoParalelo` llegan a `pasoSegmento(s, dt, 0, s.length)`. Los dos recorren el mismo arreglo con `Array.tabulate`. Como ahí no se reparte trabajo, esperamos una aceleración cercana a 1.

El control dio 0.984 para 500 000 partículas y 1.120 para un millón. En una ejecución anterior con `test run`, el control del millón había dado 10.514. Repetimos con `run` por separado para comprobar esa variación. Todas las filas de la tabla pertenecen a esta segunda ejecución. Como el control recorre un solo tramo, esa diferencia no se puede atribuir al reparto.

## 3. Comparación con el punto 4

Tomamos el sistema de un millón de partículas y usamos $p=16$, la cantidad de núcleos físicos. `trabajo` vale un millón en las cuatro filas. La parte que cambia al ajustar el umbral es `profundidad`.

| Umbral | Divisiones hasta el último tramo | Profundidad | Tiempo estimado en unidades del modelo | Tiempo medido (ms) |
|---:|---:|---:|---:|---:|
| 1 000 000 | 0 | 1 000 000 | 1 062 500 | 13.981 |
| 500 000 | 1 | 500 001 | 562 501 | 9.230 |
| 125 000 | 3 | 125 003 | 187 503 | 11.310 |
| 31 250 | 5 | 31 255 | 93 755 | 11.656 |

Por ejemplo, con umbral 125 000 hacen falta tres divisiones para llegar al tamaño pedido:

```text
1 000 000 → 500 000 → 250 000 → 125 000
```

La profundidad es $3+125\,000=125\,003$. Al sumar el trabajo entre los 16 núcleos obtenemos:

$$
125\,003+\frac{1\,000\,000}{16}=187\,503
$$

Al pasar de un tramo a dos, el modelo predice una mejora y el tiempo medido también baja, de 13.981 a 9.230 ms. A partir de ahí el modelo sigue bajando. En la máquina, el paso tardó 11.310 ms con ocho tramos y 11.656 ms con treinta y dos.

Una diferencia que sí podemos ver en el código es el costo de juntar los arreglos. Cada `++` crea un arreglo que reúne las dos mitades. Al pasar de tres a cinco divisiones, las partículas atraviesan más uniones antes de llegar al resultado final. También se crean más tareas y hay que esperar a que terminen. La fórmula del punto 4 cuenta una operación por partícula y una por división en cada camino. Esos gastos adicionales quedan fuera de la cuenta. La medición reúne esos costos en un solo tiempo.

## 4. Qué umbral resultó conveniente

Para 500 000 partículas, el umbral 15 625 dio el menor tiempo, 4.260 ms. Con 250 000 tardó 4.275 ms. Una diferencia de apenas 0.015 ms es pequeña frente a la variación del control. Para un millón, el mejor candidato de esta corrida fue 500 000: tardó 9.230 ms frente a los 15.655 ms del paso secuencial.

¿Por qué no sigue creciendo la ganancia al repartir más? Con umbral 125 000 se forman ocho tramos para un millón de partículas. Aunque haya 16 núcleos físicos, en esa etapa solo hay ocho tramos que mover. Más adelante quedan cuatro arreglos por reunir, luego dos y al final una sola unión. Cada vez hay menos tareas que puedan avanzar al mismo tiempo. Bajar todavía más el umbral crea trabajo para más núcleos, pero también agrega uniones.

### Estimación de la parte que no se reparte

Tomamos la mejor aceleración medida para un millón de partículas, $S\approx1.696$, y los 16 núcleos físicos. Llamamos $f$ a la parte del tiempo que se comporta como trabajo secuencial. Usamos la relación:

$$
\frac{1}{S}=f+\frac{1-f}{16}
$$

Al despejar:

$$
f=\frac{1/1.696-1/16}{1-1/16}\approx0.562
$$

La estimación da cerca del **56.2 %**. Ese porcentaje sale de los tiempos de esta corrida. Incluye el efecto de esperar resultados, reunir los arreglos y tener núcleos sin un tramo disponible. La diferencia entre las filas de control también influye en qué tanta precisión podemos darle a ese número.