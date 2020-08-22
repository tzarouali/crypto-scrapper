package cryptoscrapper.repositories

package object impl {

  implicit final class ScalaIntOps(val b: Int) extends AnyVal {
    def asJava: Integer = Integer.valueOf(b)
  }

  implicit final class ScalaBigdecimalOps(val b: BigDecimal) extends AnyVal {
    def asJava: java.math.BigDecimal = b.bigDecimal
  }

}
