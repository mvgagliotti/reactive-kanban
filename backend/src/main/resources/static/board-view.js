$(function () {
    var kanbanCol = $('.panel-body');
    kanbanCol.css('max-height', (window.innerHeight - 150) + 'px');

    var kanbanColCount = parseInt(kanbanCol.length);
    $('.container-fluid').css('min-width', (kanbanColCount * 350) + 'px');

    $('.panel-heading').click(function() {
        var $panelBody = $(this).parent().children('.panel-body');
        $panelBody.slideToggle();
    });

    var boardId = document.getElementById("board-id").innerText

    $('#loading-modal').modal('toggle');

    $.get(`/boards/${boardId}`, function(data, status){
        createBoard(data)
        $('#loading-modal').modal('toggle');
    });

});

var sourceId; //global variable used by drag-drop feature

//*********************TEST FUNCTIONS ************************************/
function testListUpdate() {

    var listId = "a606f0b0-702e-11e9-af4d-1f9687dfe538"
    var cards = [{
        id: "test-card",
        title: "Testing this card",
        description: "Amazing this is working!!!"
    }]

    updateCardList(listId, cards)
}

function execModal() {

    var modalBodyContent = `
        <form role="form" id="modal-form" onsubmit="event.preventDefault()">
            <div class="form-group">
                <label for="exampleInputEmail1">Email address</label>
                <input type="email" class="form-control"
                       id="exampleInputEmail1" placeholder="Enter email" required autofocus/>
            </div>
            <div class="form-group">
                <label for="exampleInputPassword1">Password</label>
                <input type="password" class="form-control"
                       id="exampleInputPassword1" placeholder="Password" required autofocus/>
            </div>
            <div class="checkbox">
                <label>
                    <input type="checkbox" value="true"/> Check me out
                </label>
            </div>
            <button type="submit" class="btn btn-default">Submit</button>
        </form>
    `

    showModal('my-test-modal', 'dynamic-modal-area', modalBodyContent, function(json) {
        console.log("closeCallback:" + json)
    })
}

//*********************TEST FUNCTIONS ************************************/


/**
* creates the board
*/
function createBoard(board) {

    //removing old stuff
    var myNode = document.getElementById("board-area");
    while (myNode.firstChild) {
        myNode.removeChild(myNode.firstChild);
    }

    //creating lists
    board.cardLists.forEach(function (cardList){
        addCardList(cardList.id, cardList.title, cardList.cards)
    })

}

/**
* Updates a given card list with cards passed
*/
function updateCardList(cardListId, cards) {

    //removing old stuff from list
    var myNode = document.getElementById(cardListId);
    while (myNode.firstChild) {
        myNode.removeChild(myNode.firstChild);
    }

    //adding updated cards
    for (var i=0; i<cards.length; i++) {
        var cardTemplate = newCardFromTemplate(cards[i].id, cards[i].title, cards[i].description)
        $(`#${cardListId}`).append(cardTemplate)
    }

}


/**
* Add a card to to some list
* //TODO: modificar aqui
*/
function addCard(title, description) {

    var cardId = 10; //TODO: generate

    var cardTemplate = newCardFromTemplate(cardId, title, description)

    $('#TODO').append(cardTemplate)

}

/**
* Adds a list to the board
*/
function addCardList(id, title, cards) {

    var list = newCardListFromTemplate(id, title, cards)

    $('#board-area').append(list)

}

// *********** Drag & Drop functions *****************//

function dragStart(event) {
//    sourceId = $(this).parent().attr('id');
    sourceId = event.target.parentElement.id
    dataTransfer(event).setData("text/plain", event.target.getAttribute('id'));
}

function dragOver(event) {
    event.preventDefault();
}

function drop(event)  {
    var children = event.currentTarget.children[0]
    var targetId = children.id

    var mvOper = moveOperation(event)
    console.log(mvOper)

    var elementId = dataTransfer(event).getData("text/plain");

    $('#processing-modal').modal('toggle');

    //posting data
    var boardId = document.getElementById("board-id").innerText
    $.ajax({
        type: 'POST',
        url: `/boards/${boardId}/move`,
        data: JSON.stringify(mvOper),
        success: function(data) {
            //rebuilding affected lists
            try {
                updateCardList(data.targetListId, data.targetListCards)
                if (data.targetCardId != data.sourceListId) {
                    updateCardList(data.sourceListId, data.sourceListCards)
                }
            } finally {
                $('#processing-modal').modal('toggle');
            }
        },
        error: function(error) {
            $('#processing-modal').modal('toggle');
        },
        contentType: "application/json",
        dataType: 'json'
    })


    event.preventDefault();
}

/**
* TODO:
*/
function moveOperation(dropEvent) {

    var cardId = dataTransfer(dropEvent).getData("text/plain");

    var parentEl = dropEvent.target.parentElement
    var attempts = 0;

    while (attempts < 5 && parentEl.getAttribute("draggable") == null) {
        attempts++
        parentEl = parentEl.parentElement
    }

    var targetCardId
    if (parentEl == null || parentEl.getAttribute("draggable") == null) {
        targetCardId = null
    } else {
        targetCardId = parentEl.id
    }

    return {
        fromList : sourceId,
        toList : dropEvent.currentTarget.children[0].id,
        card: cardId,
        cardAtTargetPosition: targetCardId
    }
}

//TODO: remove this, since it's not necessary anymore
function dataTransfer(event) {

    if (event.dataTransfer) {
        return event.dataTransfer
    } else if (event.originalEvent.dataTransfer) {
        return event.originalEvent.dataTransfer
    } else {
        return undefined
    }

}
