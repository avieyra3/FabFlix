/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
 function getParameters() {
    // Get request URL
    let url = window.location.href;
    return url.split('?')[1];
}

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
        rowHTML += "<a href=movielist.html?genre=" + resultData[i]["genre"] + ">" + resultData[i]["genre"] + "</a><br>";
        browseGenreElementBody.append(rowHTML);
    }

    let browseTitleElementBody = jQuery("#browse-by-title");

    for (let i = 97; i <= 122; i++) {
        let char = String.fromCharCode(i);
        console.log(char);
        let rowHTML = "";
        rowHTML += "<a href=movielist.html?title=" + char + ">" + char + "</a><br>";
        browseGenreElementBody.append(rowHTML);
      }

    for (let i = 0; i <= 9; i++) {
        let rowHTML = "";
        rowHTML += "<a href=movielist.html?title=" + i + ">" + i + "</a><br>";
        browseGenreElementBody.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
let parameters = getParameters();
console.log(parameters);

// Makes the HTTP GET request and registers on success callback function 
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/browse?" + parameters,
    success: (resultData) => handleBrowseResult(resultData)
});