/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
 function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    let title = jQuery("#movie_title");
    title.append("<h1>" + resultData[0]["movie_title"] + "</h1>");

    let movieYear = jQuery("#movie_year");
    movieYear.append("<span class=data>" + resultData[0]["movie_year"] + "</span>");

    let movieDirector = jQuery("#movie_director");
    movieDirector.append("<span class=data>" + resultData[0]["movie_director"] + "</span>");

    let movieGenres = jQuery("#movie_genres");
    let genresString = "<span class=data>";
    const genresArray = resultData[0]["movie_genres"].split("|");
    for (let i = 0; i < genresArray.length; i++) {
        genresString += "<a href=movie-list.html?request-type=genre=" + genresArray[i] + ">" + genresArray[i] + "</a>";
        if (i != genresArray.length - 1) {
            genresString += ", ";
        }
    }
    genresString += "</span>";
    movieGenres.append(genresString);

    let movieStars = jQuery("#movie_stars");
    let starsString = "<span class=data>";
    const starsIdArray = resultData[0]["star_id"].split("|");
    console.log(starsIdArray);
    const starsArray = resultData[0]["movie_stars"].split("|");
    console.log(starsArray);
    for(let i = 0; i < starsIdArray.length; i++)
    {
        starsString += '<a href="single-star.html?id=' + starsIdArray[i] + '">'
            + starsArray[i] + '</a>';
        if(i != starsIdArray.length - 1){
            starsString += ", ";
        }
    }
    starsString += "</span>";
    console.log(starsString);
    movieStars.append(starsString);

    let movieRating = jQuery("#movie_rating");
    movieRating.append("<span class=data>" + resultData[0]["movie_rating"] + "</span>");

    let cart = jQuery("#cart");
    cart.append("<span class=data>" + "<form ACTION='/api/cart' id='add-to-cart' METHOD='POST'>" +
        "<button TYPE='button' NAME='add-to-cart' VALUE='" + resultData[0]['movie_id'] + "'>Add</button></form>" + "</span>");


}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});