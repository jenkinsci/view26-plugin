view26.init();
$j(document).ready(function () {
  setTimeout(function () {
    hideNoHelp();
    disableView26Url(true);
  }, 1000)
});

function disableView26Url(disable) {
  $j("input[name='config.url']").attr("value","https://actio9.actiotech.com");
  if (disable) {
    $j("input[name='config.url']").attr('readonly', 'readonly');
  }
}

/*Hide unexpected help icon for fields, cause jenkins auto make help url of radio block inherit by our publish action help url*/
function hideNoHelp() {
  var parent = $j("div[descriptorid='com.view26.ci.plugin.action.PushingResultAction']");
  if (!parent || parent.length <= 0)
    return;
  var trNodes = parent.find("tr[class='radio-block-start '][hashelp='false'] > td[class='setting-help']");

  $j.each(trNodes, function (index) {
    var helpNode = trNodes[index];
    if (helpNode)
      helpNode.setAttribute('style', 'display:none');
  });
}
