function handleMovieResult(resultData) {
    //resultData is a JSON
    console.log("executing handleMovieResult...")

    let movieTableBodyElement = jQuery("#movie_table_body");

    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        let titleRowHTML = "<th>" + resultData[i]["movie_title"] + "</th>";
        let yearRowHTML = "<th>" + resultData[i]["movie_year"] + "</th>";
        let directorRowHTML = "<th>" + resultData[i]["movie_director"] + "</th>";
        let ratingRowHTML = "<th>" + resultData[i]["movie_rating"] + "</th>";
        console.log("<tr>" + titleRowHTML + yearRowHTML + directorRowHTML + ratingRowHTML + "</tr>");

        movieTableBodyElement.append("<tr>" + titleRowHTML + yearRowHTML + directorRowHTML + ratingRowHTML + "</tr>");
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movielist",
    success: (resultData) => {
        console.log("AJAX SUCCESS");
        handleMovieResult(resultData);
    },
    error: () => console.log("AJAX ERROR")
});