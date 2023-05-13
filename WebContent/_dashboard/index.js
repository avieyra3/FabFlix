
function handleMetaData(resultData) {
    console.log("handleMetaData: populating meta table from resultData");
    console.log(resultData);

    // Populate the meta table
    let metaData = jQuery("#metadata");

    // Iterate through each table in resultData
    for (let i = 0; i < resultData.length; i++) {

        let html = "<h2 class='meta_header'>" + resultData[i]["table"] + "</h2>";
        html += "<table id=meta_table class='table table-hover'>";
        html += "<thead class='black white-text'><tr><th>Field</th><th>DataType</th></tr></thead>";
        html += "<tbody>";

        for (let j = 0; j < resultData[i]["field"].length; j++) {
            html += "<tr><th>" + resultData[i]["field"][j] + "</th>";
            html += "<th>" + resultData[i]["type"][j] + "</th></tr>";
        }
        html += "</tbody></table>";

        // Append the row created to the table body, which will refresh the page
        metaData.append(html);
    }
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "../api/index-employee", // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
    success: (resultData) => handleMetaData(resultData) // Setting callback function to handle data returned successfully by the MovieListServlet
});