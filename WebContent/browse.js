/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
 function handleBrowseResult(resultData) {
    console.log("handleBrowseResult: creating links");

    // populate webpage with links
    let browseGenreElementBody = jQuery("#browse-by-genre");

    for (let i = 0; i < resultData.length; i++) {

        let rowHTML = "";
        rowHTML += "<a href=movie-list.html?broswe-genre=" + resultData[i]["genre"] + ">" + resultData[i]["genre"] + "</a><br>";
        browseGenreElementBody.append(rowHTML);
    }

    let browseTitleElementBody = jQuery("#browse-by-title");

    for (let i = 97; i <= 122; i++) {
        let char = String.fromCharCode(i);
        console.log(char);
        let rowHTML = "";
        rowHTML += "<a href=movie-list.html?browse-title=" + char + ">" + char + "</a><br>";
        browseTitleElementBody.append(rowHTML);
    }

    for (let i = 0; i <= 9; i++) {
        let rowHTML = "";
        rowHTML += "<a href=movie-list.html?browse-title=" + i + ">" + i + "</a><br>";
        browseTitleElementBody.append(rowHTML);
    }
    browseTitleElementBody.append("<a href=movie-list.html?browse-title=*>*</a><br>");
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/browse?",
    success: (resultData) => handleBrowseResult(resultData)
});