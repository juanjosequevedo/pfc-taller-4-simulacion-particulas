package taller

/**
 * El punto 5 pide medir aquí, sobre un sistema de al menos un millón de
 * partículas, el tiempo de `pasoSecuencial` y el de `pasoParalelo` con varios
 * umbrales, y reportar la aceleración de cada uno.
 *
 * Lo que hay ahora es el arranque mínimo sobre dos partículas.
 */
object App {

  def main(args: Array[String]): Unit = {
    val sim = new SimulacionParticulas()
    import sim._

    val sistema: Sistema = Array(
      Particula(Vector2(0, 0), Vector2(1, 1), 1.0),
      Particula(Vector2(2, 0), Vector2(0, 1), 2.0))

    println(s"Núcleos disponibles: ${Runtime.getRuntime.availableProcessors}")
    println(pasoSecuencial(sistema, 0.5).mkString(", "))

    val dt = 0.5
    println("Partículas | Umbral | Secuencial (ms) | Paralelo (ms) | Aceleración")

    List(500000, 1000000).foreach { n =>
      val particulas = Array.tabulate(n) { i =>
        val posicion = Vector2(i % 1000, i / 1000)
        val velocidad = Vector2(i % 7 - 3, i % 5 - 2)
        Particula(posicion, velocidad, 1 + i % 4)
      }

      val secuencial = medir(5)(pasoSecuencial(particulas, dt))
      val umbrales = List(n, n / 2, n / 8, n / 32)

      umbrales.foreach { umbral =>
        val paralelo = medir(5)(pasoParalelo(particulas, dt, umbral))
        val ganancia = aceleracion(secuencial, paralelo)
        println(f"$n%d | $umbral%d | $secuencial%.3f | $paralelo%.3f | $ganancia%.3f")
      }
    }
  }
}
