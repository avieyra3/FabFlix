/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
 function getParameters() {
    // Get request URL
    let url = window.location.href;
    // // Encode target parameter name to url encoding
    // target = target.replace(/[\[\]]/g, "\\$&");
    //
    // // Ues regular expression to find matched parameter value
    // let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
    //     results = regex.exec(url);
    // if (!results) return null;
    // if (!results[2]) return '';
    //
    // // Return the decoded parameter value
    // return decodeURIComponent(results[2].replace(/\+/g, " "));
    return url.split('?')[1];
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");
    console.log(resultData);

    // Populate the movie table
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" +
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] + '</a>' + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        const genresArray = resultData[i]["movie_genres"].split(", ");
        rowHTML += "<th>";
        let j = 0;
        while (j < Math.min(3 - 1, genresArray.length - 1)) {
            rowHTML += "<a href=movie-list.html?request-type=genre=" + genresArray[j] + ">" + genresArray[j] + "</a>" + ", ";
            j++;
        }
        rowHTML += "<a href=movie-list.html?request-type=genre=" + genresArray[j] + ">" + genresArray[j] + "</a>" + "</th>";
        const starsIdArray = resultData[i]["star_id"].split(", ");
        const starsArray = resultData[i]["movie_stars"].split(", ");
        rowHTML += "<th>";
        j = 0;
        while (j < Math.min(3 - 1, starsIdArray.length - 1)) {
            rowHTML += '<a href="single-star.html?id=' + starsIdArray[j] + '">' + starsArray[j] + '</a>' + ", ";
            j++;
        }
        rowHTML += '<a href="single-star.html?id=' + starsIdArray[j] + '">' + starsArray[j] + '</a>' + "</th>";

        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "<th><form ACTION='#' id='cart" + i + "' METHOD='POST'>" +
                    "<input TYPE='hidden' NAME='item' VALUE='" + resultData[i]['movie_id'] + "'>" +
                    "<input TYPE='submit' VALUE='Add'>" +
                    "</form></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);

        let cart = $("#cart" + i);
        console.log(cart);
        cart.submit(handleCartInfo);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let parameters = getParameters();
console.log(parameters);

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movielist?" + parameters, // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MovieListServlet
});


function handleCartInfo(cartEvent) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();

    $.ajax("api/cart", {
        method: "POST",
        data: $(this).serialize(),
        success: function () {
            console.log("Movie Successfully Added!");
            alert("Movie Successfully Added!");
        },
        error: function () {
            console.log("Failed To Add Movie");
            alert("Failed To Add Movie");
        }
    });
}

