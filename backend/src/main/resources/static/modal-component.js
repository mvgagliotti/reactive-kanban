/**
*
*/
function showModal(modalId, placementId, bodyContent, closeCallback) {

    //builds the modal
    var modalContent = modalDialog(modalId, bodyContent)
    $(`#${placementId}`).append(modalContent)

    //shows the modal
    $(`#${modalId}`).modal()
    $(`#${modalId}`).modal('show');

    //binding submit event to prevent form submition and close modal if all input are valid
    $(`#${modalId} .modal-body form`).on("submit", function(e) {

        var allValid = true;
        var inputs = $(`#${modalId} .modal-body input`).toArray()
        for (var i=0; i<inputs.length; i++) {
            allValid &= inputs[i].validity ? inputs[i].validity.valid : false
        }

        if (allValid) {
            $(`#${modalId}`).modal('hide')
        }

        e.preventDefault()
    })

    //binding click function to save button, so it submit the form a close the modal if all inputs are valid
    $(`#${modalId} .modal-footer .save`).on("click", function() {
        var form = $(`#${modalId} .modal-body form`)
        if (form && form.length > 0) {
           form.submit()
        }
    })

    //on modal closing
    $(`#${modalId}`).on('hide.bs.modal', function(){

        var jsonForm = {}

        $(`#${modalId} .modal-body input`)
            .toArray()
            .forEach(function (x) {
                console.log(x.id + "->" + x.value)
                jsonForm[x.id] = x.value
            })

        $(`#${modalId}`).remove()

        if (closeCallback) {
            closeCallback(jsonForm)
        }

    });
}

/**
* Creates a modal template
*
* @param modalId the modal id
*/
function modalDialog(modalId, bodyContent) {

    var dialog =
        `
        <div class="modal fade" id="${modalId}" tabindex="-1" role="dialog"
             aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <!-- Modal Header -->
                    <div class="modal-header">
                        <button type="button" class="close"
                                data-dismiss="modal">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only">Close</span>
                        </button>
                        <h4 class="modal-title" id="myModalLabel">
                            Modal title
                        </h4>
                    </div>

                    <!-- Modal Body -->
                    <div class="modal-body">
                        ${bodyContent}
                    </div>

                    <!-- Modal Footer -->
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default"
                                data-dismiss="modal">
                            Close
                        </button>
                        <button type="button" class="btn btn-primary save">
                            Save <changes></changes>
                        </button>
                    </div>
                </div>
            </div>
        </div>
        `
    return dialog
}