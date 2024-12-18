function handleLeftClick(cellID){
    sendClickToServer(cellID, "TRY");
}

function handleRightClick(event, cellID){
    event.preventDefault();
    sendClickToServer(cellID, "FLAG"); 
}

function sendClickToServer(cellId, actionType) {
    var data = {
        cellId: cellId,
        actionType: actionType
    };

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/your-server-endpoint", true);
    xhr.setRequestHeader("Content-Type", "application/json");

    xhr.onload = function () {
        if (xhr.status === 200) {
            // Traitement de la réponse si nécessaire
            console.log('Réponse du serveur:', xhr.responseText);
        } else {
            console.log('Erreur lors de l\'envoi des données au serveur');
        }
    };

    // Envoi les données en JSON
    xhr.send(JSON.stringify(data));
}