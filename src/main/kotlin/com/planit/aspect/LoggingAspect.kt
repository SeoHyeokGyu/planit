package com.planit.aspect

import jakarta.servlet.http.HttpServletRequest
import java.util.*
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class LoggingAspect {

  private val logger = LoggerFactory.getLogger(LoggingAspect::class.java)

  // 컨트롤러 패키지 내의 모든 메소드를 대상으로 함
  @Pointcut("execution(* com.planit.controller..*(..))") fun controllerMethods() {}

  @Around("controllerMethods()")
  fun logApiCall(joinPoint: ProceedingJoinPoint): Any? {
    val uuid = UUID.randomUUID().toString().substring(0, 8)
    val request =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

    val startTime = System.currentTimeMillis()

    // --- Request Logging ---
    logRequest(uuid, request, joinPoint)

    return try {
      val result = joinPoint.proceed()

      // --- Response Logging ---
      val duration = System.currentTimeMillis() - startTime
      logResponse(uuid, result, duration)

      result
    } catch (e: Exception) {
      val duration = System.currentTimeMillis() - startTime
      logError(uuid, e, duration)
      throw e
    }
  }

    private fun logRequest(uuid: String, request: HttpServletRequest?, joinPoint: ProceedingJoinPoint) {
        val method = request?.method ?: "알수없음"
        val uri = request?.requestURI ?: "알수없음"
        val methodName = joinPoint.signature.name
        val args = joinPoint.args.map { arg ->
            // 파일 업로드 객체 등 로그에 찍기 부적절한 타입 필터링
            when (arg) {
                is org.springframework.web.multipart.MultipartFile -> "파일(${arg.originalFilename}, ${arg.size} 바이트)"
                is HttpServletRequest -> "HttpServletRequest"
                is jakarta.servlet.http.HttpServletResponse -> "HttpServletResponse"
                else -> arg.toString()
            }
        }

        logger.info("[$uuid] [요청] -> $method $uri | 메서드: $methodName | 파라미터: $args")
    }

    private fun logResponse(uuid: String, result: Any?, duration: Long) {
        val resultString = result?.toString()?.let {
            if (it.length > 1000) "${it.substring(0, 1000)}... (생략됨)" else it
        } ?: "null"
        
        logger.info("[$uuid] [응답] <- $resultString | 처리시간: ${duration}ms")
    }

    private fun logError(uuid: String, e: Exception, duration: Long) {
        logger.error("[$uuid] [에러] <- ${e.javaClass.simpleName}: ${e.message} | 처리시간: ${duration}ms")
    }
}
