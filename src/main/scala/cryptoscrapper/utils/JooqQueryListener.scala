package cryptoscrapper.utils

import java.lang.Boolean.TRUE
import java.util

import cryptoscrapper.newtypes.TraceId
import org.jooq.impl.DSL.`val`
import org.jooq.impl.{DSL, DefaultExecuteListener, DefaultVisitListener, DefaultVisitListenerProvider}
import org.jooq.tools.StringUtils.abbreviate
import org.jooq.{Configuration, ExecuteContext, Param, QueryPart, VisitContext, VisitListenerProvider}

class JooqQueryListener(tid: TraceId) extends DefaultExecuteListener with ApplicationLogger {

  private val log = new Log

  override def renderEnd(ctx: ExecuteContext): Unit = {
    var configuration: Configuration = ctx.configuration
    val newline: String = if (TRUE == configuration.settings.isRenderFormatted) "\n" else ""
    configuration = abbreviateBindVariables(configuration)
    if (ctx.query != null) {
      val inlined: String = DSL.using(configuration).renderInlined(ctx.query)
      if (!(ctx.sql == inlined)) log.debug("Executing query: " + newline + inlined)(tid)
    } else {
      if (ctx.routine != null) {
        val inlined: String = DSL.using(configuration).renderInlined(ctx.routine)
        if (!(ctx.sql == inlined)) log.debug("Executing query: " + newline + inlined)(tid)
      }
    }
  }

  private def abbreviateBindVariables(configuration: Configuration) = {
    val oldProviders = configuration.visitListenerProviders
    val newProviders = new Array[VisitListenerProvider](oldProviders.length + 1)
    System.arraycopy(oldProviders, 0, newProviders, 0, oldProviders.length)
    newProviders(newProviders.length - 1) = new DefaultVisitListenerProvider(new BindValueAbbreviator)
    configuration.derive(newProviders:_*)
  }

  override def exception(ctx: ExecuteContext): Unit = {
    log.debug("JOOQ Exception " + ctx.exception)(tid)
  }


  private class BindValueAbbreviator extends DefaultVisitListener {
    private val maxLength = 2000

    override def visitStart(context: VisitContext): Unit = {
      if (context.renderContext != null) {
        val part: QueryPart = context.queryPart
        part match {
          case param: Param[_] =>
            val value: Any = param.getValue
            value match {
              case str: String if str.length > maxLength =>
                context.queryPart(`val`(abbreviate(str, maxLength)))
              case bytes: Array[Byte] if bytes.length > maxLength =>
                context.queryPart(`val`(util.Arrays.copyOf(bytes, maxLength)))
              case _ =>
            }
          case _ =>
        }
      }
    }
  }

}
