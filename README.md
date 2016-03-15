# popular-movies
<img src="https://github.com/dashengz/popular-movies/blob/master/screenshots/popularmovies_thumb.png" width="300px" />

### Project Description:
Udacity Android Nanodegree Project 1

PopularMovies is a mobile app (also optimized for tablets) that helps users discover popular and highly rated movies on the web.
It displays a scrolling grid of movie trailers, launches a details screen whenever a particular movie is selected,
allows users to save favorites, play trailers, and read user reviews.

This app utilizes core Android user interface components and fetches movie information using themoviedb.org web API.
It also uses its own content provider and SQLite database for data storage.

### How to use:
**The Movie Database API Key is required.**

Please obtain a key following the [instructions](https://www.themoviedb.org/faq/api).

Include the unique API key in [Project Root Folder]/.gradle/gradle.properties

`TheMovieDatabaseApiKey="<YOUR_API_KEY_HERE>"`

### Screenshots of the App:
Browse through currently popular movies via a gridlayout:

<img src="https://github.com/dashengz/popular-movies/blob/master/screenshots/home.png" width="200px" />
<img src="https://github.com/dashengz/popular-movies/blob/master/screenshots/detail_tablet_port.png" height="333px" />

Click on a poster to view the details:

<img src="https://github.com/dashengz/popular-movies/blob/master/screenshots/detail.png" width="200px" />
<img src="https://github.com/dashengz/popular-movies/blob/master/screenshots/detail_tablet_land.png" height="333px" />

Three different filters available:

<img src="https://github.com/dashengz/popular-movies/blob/master/screenshots/settings.png" width="200px" />
<img src="https://github.com/dashengz/popular-movies/blob/master/screenshots/sortby.png" width="200px" />

User-friendly alerts:

<img src="https://github.com/dashengz/popular-movies/blob/master/screenshots/noinfo.png" width="200px" />