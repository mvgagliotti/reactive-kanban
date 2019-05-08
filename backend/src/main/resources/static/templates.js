/**
* Returns a string of card
*/
function newCardFromTemplate(cardId, title, description) {
    var result = `<div class="card grab" id="${cardId}" draggable="true" ondragstart="dragStart(event)">
                   <div class="card-inner">
                        <div class="card-label">
                            <h2>${title}</h2>
                            <p>${description}</p>
                        </div>
                    </div>
                 </div>`
    return result
}

/**
* Returns a string of a card list
*/
function newCardListFromTemplate(cardListId, title, cards) {

    var cardPanels = ""
    for (var i=0; i<cards.length; i++) {
        cardPanels = cardPanels + newCardFromTemplate(cards[i].id, cards[i].title, cards[i].description)
    }

    var result = `<div class="panel panel-primary card-list-panel">
                         <div class="panel-heading">
                             ${title}
                             <i class="fa fa-2x fa-plus-circle pull-right"></i>
                         </div>
                         <div id="panel-body${cardListId}" class="panel-body" ondragover="dragOver(event)" ondrop="drop(event)">
                             <div id="${cardListId}" class="card-container">
                                 ${cardPanels}
                             </div>
                         </div>
                         <div class="panel-footer">
                             <a href="#">Add a card...</a>
                         </div>
                     </div>`


    return result

}
