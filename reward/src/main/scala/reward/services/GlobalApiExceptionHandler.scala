package reward.services

import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation.{ExceptionHandler, RestControllerAdvice}
import org.springframework.web.context.request.WebRequest
import reward.pages.ApiError

/**
  * global rest controller exception
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@RestControllerAdvice
class GlobalApiExceptionHandler{

  @ExceptionHandler(Array(classOf[Exception]))
  def globalExcpetionHandler(ex: Exception, request: WebRequest): ResponseEntity[ApiError] = {
    val errorDetails = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage)
    ex.printStackTrace()
    new ResponseEntity[ApiError](errorDetails, HttpStatus.INTERNAL_SERVER_ERROR)
  }
}
