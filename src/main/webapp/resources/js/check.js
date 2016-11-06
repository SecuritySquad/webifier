/**
 * Created by samuel on 02.11.16.
 */
var stomp = Stomp.over(new SockJS('/connect'));
stomp.debug = function (args) {
    // console.log(args);
};
stomp.connect(header, function (frame) {
    console.log(frame);
    stomp.subscribe('/user/checked', function (payload, headers, res) {
        console.log(payload);
        if (!payload) {
            location.reload();
        }
    }, header);
}, function (err) {
    console.error('connection error:' + err);
});