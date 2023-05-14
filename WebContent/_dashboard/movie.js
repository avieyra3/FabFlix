$('#movie-form').on('submit', function(event) {
    // Prevent the form from being submitted
    event.preventDefault();

    // Get the values from the form
    let title = $('#movie-title').val();
    let year = $('#year').val();
    let director = $('#director').val();
    let starName = $('#star').val();
    let birthYear = $('#star-year').val() || null; // Assign to null if empty
    let genre = $('#genre').val();
    console.log("title: "+ title + " Year: " + year + " Director: " + director +
        " star name: " + starName + " birth year: " + birthYear + " genre: " + genre);

    // Prepare data to send to the server
    let data = {
        title: title,
        year: year,
        director: director,
        starName: starName,
        birthYear: birthYear,
        genre: genre
    };
    console.log("data: " + JSON.stringify(data));
    // Send a POST request to your server
    $.ajax({
        type: 'POST',
        url: '../api/add-movie',
        data: data,
        success: function(data) {
            console.log('Response:', data);
            if (data.status === 'success')
                console.log('Movie added successfully.');

            $('#movie-title').val('');
            $('#year').val('');
            $('#director').val('');
            $('#star').val('');
            $('#star-year').val('');
            $('#genre').val('');
            let newData = {
                starName: starName,
                genre: genre
            };
            console.log("data: " + JSON.stringify(newData));
            $('#message-report').text("Success! Movie Added!")
                $.get('../api/add-movie', newData, function(getData) {
                    console.log('newMovieId: ' + getData["newMovieId"] +
                        " newStarId: " + getData["newStarId"] + " newGenreId: " +
                        getData["newGenreId"]);

                    $('#movie-id').text('newMovieId: ' + getData["newMovieId"] +
                        " newStarId: " + getData["newStarId"] + " newGenreId: " +
                        getData["newGenreId"]);
                });
        },
        error: function(error) {
            console.error('Error:', error);
            $('#message-report').text("Cannot add Movie, Movie already exists!")
        }
    });
});