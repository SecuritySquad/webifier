var updateQueue = function () {
    $('#queue-size-item').removeClass('hidden');
    $.getJSON("/queue", {}, function (queue) {
        $('#queue-size').html(queue.size);
    });
};
updateQueue();
setInterval(updateQueue, 10000);