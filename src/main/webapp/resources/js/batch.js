$('#open-batch-file').click(function (event) {
    event.preventDefault();
    $('#open-batch-file-input').click();
});
$('#open-batch-file-input').change(function (event) {
    var file = event.target.files[0];
    if (!file) {
        return;
    }
    var reader = new FileReader();
    reader.onload = function(e) {
        var contents = e.target.result;
        $('.input-webifier-batch').text(contents);
    };
    reader.readAsText(file);
});