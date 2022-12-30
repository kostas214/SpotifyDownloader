import spotipy
from spotipy.oauth2 import SpotifyClientCredentials
import string
import music_tag
import requests
import SpotifyApiCredentials
import time
from youtubesearchpython import VideosSearch


sp = spotipy.Spotify(client_credentials_manager=SpotifyClientCredentials(client_id=SpotifyApiCredentials.SPOTIPY_CLIENT_ID,
                                                                         client_secret=SpotifyApiCredentials.SPOTIPY_CLIENT_SECRET,
                                                                         ))

Failed = []
def songSearchSpotify(playlistLink):
    #Initialize Spotipy
    #Store the songs of the playlist in a list
    done = True
    offset = 0
    songs = []
    tracks = sp.playlist_items(playlist_id=playlistLink, offset=offset)
    for key in tracks['items']:
        songs.append(f"{key['track']['name']} {key['track']['artists'][0]['name']}")
    while done:
        if len(songs) == offset + 100:
            tracks = sp.playlist_items(playlist_id=playlistLink, offset=offset)
            for key in tracks['items']:
                songs.append(f"{key['track']['name']} {key['track']['artists'][0]['name']}")
            offset += 100
        if len(songs) < offset + 100:
            done = False

    return songs





def DownloadSongs(songs,filePath):
    urlTemplateForServerLocalHost = "http://192.168.2.17:5000/?link="

    songSearch = VideosSearch(songs, limit = 1).result()
    songId = songSearch['result'][0]['id']
    songTitle = songSearch['result'][0]['title']


    translation_table = str.maketrans('', '', string.punctuation)
    safeString = songTitle.translate(translation_table)
    fileName = safeString.replace(" ", "") + ".aac"


    tracks = sp.search(songs)


    ArtWorkURL = tracks['tracks']['items'][0]['album']['images'][0]['url']
    albumName = tracks['tracks']['items'][0]['album']['name']
    albumArtistName = tracks['tracks']['items'][0]['album']['artists'][0]['name']
    albumTrackCount = tracks['tracks']['items'][0]['album']['total_tracks']
    albumTrackNumber = tracks['tracks']['items'][0]['track_number']
    releaseDate = tracks['tracks']['items'][0]['album']['release_date'][0:4]
    artistName = tracks['tracks']['items'][0]['artists'][0]['name']
    trackName = tracks['tracks']['items'][0]['name']




    st = time.time()

    urlForServer = urlTemplateForServerLocalHost + songId
    fileLocation = fr"{filePath}/{fileName}"
    songData = requests.get(url = urlForServer)
    open(fileLocation,'wb').write(songData.content)

    et = time.time()
    elapsed_time = et - st
    if elapsed_time>30:
        Failed.append(songs)
    print(f'Execution time: {elapsed_time} seconds')
    print(f"Downloads Failed {Failed}, Lenght of array  {len(Failed)}")

    response = requests.get(ArtWorkURL)
    f = music_tag.load_file(fileLocation)
    f['album'] = albumName
    f['albumartist'] = albumArtistName
    f['artist'] = artistName
    f['artwork'] = response.content
    f['totaltracks'] = albumTrackCount
    f['tracknumber'] = albumTrackNumber
    f['tracktitle'] = trackName
    f['year'] = releaseDate
    f.save()

