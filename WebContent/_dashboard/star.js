$('#star-form').on('submit', function(event) {
    // Prevent the form from being submitted
    event.preventDefault();

    // Get the values from the form
    let starName = $('#star-name').val();
    let birthYear = $('#birth-year').val() || null; // Assign to null if empty
    console.log("star name: " + starName + " birth year: " + birthYear);
    // Prepare data to send to the server
    let data = {
        starName: starName,
        birthYear: birthYear
    };
    console.log(data);
    // Send a POST request to your server
    $.ajax({
        type: 'POST',
        url: '../api/add-star',
        data: data,
        success: function(data) {
            console.log('Success:', data);
            $('#star-name').val('');
            $('#birth-year').val('');
            $('#message-report').text("success! Star Added!")
            $('#star-id').text('New Star ID: ' + data.id);
        },
        error: function(error) {
            console.error('Error:', error);
            $('#message-report').text("Error! Star was not added")
        }
    });
});
