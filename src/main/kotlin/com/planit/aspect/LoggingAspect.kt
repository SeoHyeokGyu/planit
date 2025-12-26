package com.planit.aspect

import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Aspect
@Component
class LoggingAspect {

  private val logger = LoggerFactory.getLogger(LoggingAspect::class.java)

  companion object {
    private const val MAX_RESULT_LENGTH = 1000
    private const val UUID_LENGTH = 8
    private const val MDC_KEY = "requestId"
  }

  @Pointcut("execution(* com.planit.controller..*(..))") fun controllerMethods() {}

  @Pointcut("execution(* com.planit.service..*(..))") fun serviceMethods() {}

  @Around("controllerMethods()")
  fun logApiCall(joinPoint: ProceedingJoinPoint): Any? {
    val uuid = getOrInitUuid()
    val request =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
    val methodInfo = "${request?.method ?: "UNKNOWN"} ${request?.requestURI ?: ""}"

    try {
      // Controller는 요청의 진입점이므로 HTTP Method와 URI 정보를 추가로 넘김
      return executeLogging(
          layer = "Controller",
          uuid = uuid,
          joinPoint = joinPoint,
          extraInfo = methodInfo,
      )
    } finally {
      // 요청 처리가 끝나면 반드시 MDC 정리
      MDC.clear()
    }
  }

  @Around("serviceMethods()")
  fun logServiceCall(joinPoint: ProceedingJoinPoint): Any? {
    val uuid = getOrInitUuid()
    return executeLogging(
        layer = "Service",
        uuid = uuid,
        joinPoint = joinPoint,
    )
  }

  /** 로깅과 실행을 담당하는 공통 메소드 */
  private fun executeLogging(
      layer: String,
      uuid: String,
      joinPoint: ProceedingJoinPoint,
      extraInfo: String? = null,
  ): Any? {
    val className = joinPoint.signature.declaringType.simpleName
    val methodName = joinPoint.signature.name
    val fullMethodName = "$className.$methodName"

    val argsString = formatArgs(joinPoint.args)
    val prefix = if (!extraInfo.isNullOrBlank()) "$extraInfo | " else ""

    // [요청] 로그
    logger.debug("[$uuid] [$layer 요청] -> $prefix$fullMethodName | 파라미터: $argsString")

    val startTime = System.currentTimeMillis()
    try {
      val result = joinPoint.proceed()

      val duration = System.currentTimeMillis() - startTime
      val resultString = formatResult(result)

      // [응답] 로그
      logger.debug(
          "[$uuid] [$layer 응답] <- $fullMethodName | 결과: $resultString | 처리시간: ${duration}ms"
      )
      return result
    } catch (e: Exception) {
      val duration = System.currentTimeMillis() - startTime
      // [에러] 로그
      logger.error(
          "[$uuid] [$layer 에러] <- $fullMethodName | ${e.javaClass.simpleName}: ${e.message} | 처리시간: ${duration}ms"
      )
      throw e
    }
  }

  private fun getOrInitUuid(): String {
    return MDC.get(MDC_KEY)
        ?: run {
          val newUuid = UUID.randomUUID().toString().take(UUID_LENGTH)
          MDC.put(MDC_KEY, newUuid)
          newUuid
        }
  }

  private fun formatArgs(args: Array<Any>): String {
    return args.joinToString(", ") { arg ->
      when (arg) {
        null -> "null"
        is MultipartFile -> "file(${arg.originalFilename}, ${arg.size}bytes)"
        is HttpServletRequest -> "HttpServletRequest"
        is jakarta.servlet.http.HttpServletResponse -> "HttpServletResponse"
        else -> arg.toString()
      }
    }
  }

  private fun formatResult(result: Any?): String {
    if (result == null) return "null"
    val str = result.toString()
    return if (str.length > MAX_RESULT_LENGTH) {
      "${str.take(MAX_RESULT_LENGTH)}... (생략됨)"
    } else {
      str
    }
  }
}
