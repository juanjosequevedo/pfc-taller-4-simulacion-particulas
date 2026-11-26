Para revisar las funciones empezamos por una sola partícula. Después vemos qué ocurre al recorrer varias y al dividir el arreglo en dos. En las versiones paralelas hay dos preguntas que debemos responder. ¿Cada partícula aparece una sola vez? ¿Qué operación permite reunir las respuestas sin cambiar su significado?

Usamos $n$ para la cantidad de partículas y $[d,h)$ para el tramo que empieza en $d$ y termina justo antes de $h$. Trabajamos con índices válidos, de modo que $0\le d\le h\le n$.

## 1. Corrección del movimiento

### `mover`

Si una partícula está en la posición $x$ y lleva velocidad $v$, durante un tiempo $dt$ su posición cambia en $v\cdot dt$. El resultado esperado es:

$$
(x+v\cdot dt,\ v,\ m)
$$

La primera parte de `Particula(p.pos + p.vel * dt, p.vel, p.masa)` calcula esa posición. Los otros dos campos reciben la velocidad y la masa originales. Si `dt` vale cero, el desplazamiento también vale cero.

### `pasoSegmento` y `pasoSecuencial`

Un tramo $[d,h)$ contiene $h-d$ partículas. `Array.tabulate` crea esa cantidad de lugares. Para llenar el lugar $j$, se mueve la partícula del índice $d+j$.

Por ejemplo, en $[2,5)$ hay tres lugares. Los índices locales 0, 1 y 2 corresponden a los índices originales 2, 3 y 4. Cada uno aparece una vez y conserva su orden.

En general, el primer índice es $d$ y el último es $d+(h-d-1)=h-1$. Si $d=h$, la longitud es cero y se obtiene un arreglo vacío.

`pasoSecuencial` pide el tramo $[0,n)$. Por lo anterior, mueve todas las partículas y cada una queda en el lugar que le corresponde.

### `pasoParalelo`

**Caso base.** Cuando la longitud cabe en el límite, `aux` llama a `pasoSegmento`. Ese caso cubre también los tramos vacíos y los de una sola partícula.

**Qué ocurre al dividir.** Sea $k$ el índice del medio. Los dos llamados reciben $[d,k)$ y $[k,h)$. El primero termina antes de $k$ y el segundo empieza en $k$. Por ejemplo, al partir $[0,5)$ quedan los índices `0, 1` a un lado y `2, 3, 4` al otro.

Las longitudes de ambas partes suman la original:

$$
(k-d)+(h-k)=h-d
$$

Suponemos que cada llamado mueve bien su tramo. Como entre los dos cubren todos los índices y ninguno aparece en ambos, `izquierda ++ derecha` reconstruye el paso completo.

La unión de arreglos es asociativa. Esto quiere decir que, dados tres tramos A, B y C, juntar primero A con B o primero B con C deja los elementos en el mismo orden:

$$
(A \mathbin{++} B)\mathbin{++} C
=
A\mathbin{++}(B\mathbin{++} C)
$$

**Por qué termina.** El límite es al menos 1. Cuando hace falta dividir, cada hijo tiene menos elementos que su padre. Después de suficientes divisiones, todos los tramos caben en el límite.

## 2. Corrección de las magnitudes secuenciales

### `energiaSecuencial`

Después de leer $j$ partículas, la suma debe contener la energía de esas primeras $j$:

$$
E_j=\sum_{i=0}^{j-1}\frac{1}{2}m_i\lVert v_i\rVert^2
$$

Al comienzo se han leído cero elementos y la suma vale cero. En el paso siguiente se calcula la rapidez de la partícula y se agrega $\frac{1}{2}m_j\lVert v_j\rVert^2$. Así se pasa de $E_j$ a $E_{j+1}$. Cuando termina el arreglo, se tiene la energía de las $n$ partículas.

### `masaTotal`

El recorrido empieza en `0.0`. Cada paso agrega la masa de una partícula, de modo que después de $j$ elementos lleva:

$$
M_j=\sum_{i=0}^{j-1}m_i
$$

Al llegar a $n$, cada masa se ha contado una vez. Con el arreglo vacío, la suma inicial ya es la respuesta.

### `centroDeMasaSecuencial`

Durante el recorrido guardamos dos cantidades:

$$
Q_j=\sum_{i=0}^{j-1}m_i x_i
\qquad
M_j=\sum_{i=0}^{j-1}m_i
$$

Aquí $x_i$ representa la posición completa, con sus dos coordenadas. Al inicio la pareja es `((0, 0), 0)`. Al leer otra partícula se añade su posición multiplicada por su masa a $Q_j$, y su masa a $M_j$. Por eso los dos datos siguen correspondiendo a las partículas que ya pasaron.

Al final, para $M_n\ne0$, multiplicar $Q_n$ por $1/M_n$ da el centro pedido. Si la masa total es cero, la función devuelve el origen, como indica el enunciado.

## 3. Corrección de las magnitudes en paralelo

### `energiaParalela`

Cuando un tramo cabe en el umbral, `sumar` recorre sus índices y obtiene la energía de esa parte. Si se divide en $k$, cada partícula aporta a uno solo de los dos lados. Por tanto:

$$
E_{[d,h)}=E_{[d,k)}+E_{[k,h)}
$$

La suma es asociativa en los números reales. Para tres aportes se cumple $(a+b)+c=a+(b+c)$. Podemos calcular primero las sumas pequeñas y luego reunirlas. Al volver al tramo $[0,n)$ obtenemos la energía del sistema.

### `centroDeMasaParalelo`

Cada llamado devuelve una pareja $(Q,M)$ con la suma de posiciones por masa y la masa de su tramo. Para juntar dos partes se hace:

$$
(Q_1,M_1)+(Q_2,M_2)=(Q_1+Q_2,\ M_1+M_2)
$$

Al reunir tres partes, cualquiera de las dos agrupaciones deja $Q_1+Q_2+Q_3$ en el primer lugar y $M_1+M_2+M_3$ en el segundo. Esa es la razón por la que el reparto funciona. La división se hace una sola vez, cuando ya se conoce la masa total.

**Por qué no basta con promediar los centros.** Pensemos en dos grupos. El primero tiene masa 1 y su centro está en $x=0$. El segundo tiene masa 3 y su centro está en $x=8$. Si promediamos las posiciones de los centros, da $(0+8)/2=4$. Al tener en cuenta lo que pesa cada grupo obtenemos:

$$
\frac{0\cdot1+8\cdot3}{1+3}=6
$$

El centro queda más cerca del grupo de mayor masa. Si guardáramos solamente 0 y 8, perderíamos la información que permite obtener ese 6.

Las dos funciones paralelas terminan por la misma razón que `pasoParalelo`: cada división reduce el tramo pendiente. En el programa se usa `Double`, así que al cambiar el orden de las sumas puede variar algún decimal por redondeo. Por eso para estas magnitudes se comparan los resultados con una pequeña tolerancia.

## 4. Corrección de los costos

### `trabajo`

Hay un movimiento por partícula. Repartir el arreglo cambia quién hace cada parte, pero las $n$ partículas siguen presentes. Así, $W(n)=n$ y `trabajo(0)` vale cero.

### `profundidad`

Si un tramo cabe en el umbral, se recorre completo y su costo es su longitud. Si se divide, contamos una división y esperamos a que terminen ambos lados. Llamando $u$ al límite efectivo, la cuenta que buscamos es:

$$
D(n,u)=
\begin{cases}
n, & n\leq u,\\
1+\max\bigl(D(\lfloor n/2\rfloor,u),D(\lceil n/2\rceil,u)\bigr), & n>u.
\end{cases}
$$

El código calcula esta cuenta por niveles. `particiones` indica cuántas divisiones llevamos y `tramos` cuántas partes se formaron a esa altura. Al partir siempre por el medio, sus tamaños difieren a lo sumo en uno. Por eso basta conocer `menor` y `mayor`.

- Si `mayor` cabe, todos caben. El costo que queda es el tamaño del mayor.
- Si solo cabe `menor`, el otro tiene exactamente un elemento más que el límite. Con una división adicional, sus dos mitades ya caben. Se compara el costo de esa división y su mitad mayor con lo que tarda recorrer `menor`.
- Si ambos superan el límite, todos se vuelven a partir. Se duplica el número de tramos y se suma una división al recorrido.

Así se cuenta el camino que más tarda. Por ejemplo, con 17 partículas y umbral 8, el tramo de 8 tarda más que partir el de 9 y recorrer su mitad de 5. La profundidad total es 9. Este caso explica por qué el código hace la comparación entre los dos lados.

### `tiempoEstimado`

Para $p>0$ procesadores, el enunciado pide:

$$
T(n,p,u)=D(n,u)+\frac{W(n)}{p}
$$

La función usa los dos resultados anteriores. `toDouble` conserva la parte decimal del cociente. Por ejemplo, con 17 partículas y cuatro procesadores, el aporte del trabajo es `4.25`.

## 5. Corrección de la medición y la aceleración

`medir` ordena las muestras y toma el elemento de la posición `length / 2`. Con las cinco repeticiones que usa `umbralConveniente`, esa posición es 2 contando desde cero. Quedan dos muestras antes y dos después, de modo que el valor elegido es la mediana.

Si el tiempo paralelo es positivo, `aceleracion` calcula:

$$
S=\frac{T_{\mathrm{secuencial}}}{T_{\mathrm{paralelo}}}
$$

Para un mismo trabajo, $S>1$ indica que terminó antes al repartirlo. Si ambos tiempos coinciden, $S=1$.

En `umbralConveniente`, cada candidato queda acompañado del tiempo que se obtuvo al probarlo. `minBy(_._2)` escoge la pareja con el menor tiempo medido y `._1` devuelve su umbral. Si dos candidatos empatan, el elegido sigue teniendo el menor tiempo. Para una lista vacía se devuelve `s.length`, que permite resolver el sistema completo en un tramo.