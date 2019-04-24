var stompClient = null;

function connectAndSubscribe() {
    var socket = new SockJS('/reactive-kanban');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        var boardId = $('#main-content table:first')[0].id
        console.log('Monitoring board '+ boardId)
        stompClient.subscribe('/topic/board-updates/' + boardId, function (stompMessage) {
            addMessage(stompMessage.body);
        });
    });
}

function addMessage(message) {
    $("#userinfo").append("<tr><td>" + message + "</td></tr>");
}
