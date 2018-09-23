package com.example

import scala.xml.Elem

class HtmlTemplate {
  def render(evaluationResult: EvaluationResult) = evaluationResult match {
    case s @ Success(_, _)   => renderSuccess(s)
    case f @ Failure(_,_) => renderFailed(f)
  }

  private def renderSuccess(success: Success) =
    <html>
      <h1>
        Your exercise was evaluated with <b> Success! </b>
      </h1>
      <ul>
        {renderItems(success.testReport)}
      </ul>
    </html>

  private def renderFailed(failure: Failure)   =
    <html>
      <h1>
        Your exercise was evaluated with <b> Failure {failure.exitCode}! </b>
      </h1>
      <ul>
        {renderItems(failure.testReport)}
      </ul>
    </html>


  private def renderItems(items:List[TestResult]): List[Elem] =
    items.map{
      case Succeeded(duration,name) =>
        <li><font color="green">Success: <b>{name}</b>, finished in {duration}</font></li>
      case Failed(errorMessage,duration,name) =>
        <li><font color="red">Failed: <b>{name}</b>, in {duration} with message: <p>{errorMessage}</p></font></li>
    }


}
