import httpx
import music_tag
import requests
import spotipy
import string
import time
from httpx import ReadError
from requests.exceptions import ChunkedEncodingError
from spotipy.oauth2 import SpotifyClientCredentials
from youtubesearchpython import VideosSearch
import mutagen

import SpotifyApiCredentials

sp = spotipy.Spotify(client_credentials_manager=SpotifyClientCredentials(
    client_id=SpotifyApiCredentials.SPOTIPY_CLIENT_ID,
    client_secret=SpotifyApiCredentials.SPOTIPY_CLIENT_SECRET,
))

Failed = []


def songSearchSpotify(playlistLink):
    # Initialize Spotipy
    # Store the songs of the playlist in a list
    success = 0
    done = True
    offset = 0
    songs = []
    try:
        tracks = sp.playlist_items(playlist_id=playlistLink, offset=offset)
    except requests.exceptions.ConnectionError:
        success = 1
        print("Unable to connect to the internet")
    except spotipy.exceptions.SpotifyException:
        success = 2
        print("Invalid Link")

    if success == 0:
        for key in tracks['items']:
            songs.append(f"{key['track']['name']} {key['track']['artists'][0]['name']}")
        while done:
            if len(songs) == offset + 100:
                try:
                    tracks = sp.playlist_items(playlist_id=playlistLink, offset=offset)
                except requests.exceptions.ConnectionError:
                    success = 1
                    print("unable to connect to the internet")
                for key in tracks['items']:
                    songs.append(f"{key['track']['name']} {key['track']['artists'][0]['name']}")
                offset += 100
            if len(songs) < offset + 100:
                done = False

        return songs, success

    else:
        print("unable to get songs")
        return songs, success





def getDownloadPath(songs):
    fileName = ""
    songUrl=""


    try:
        songSearch = VideosSearch(songs, limit=1).result()
        songId = songSearch['result'][0]['id']
        songUrl= "https://youtu.be/"+songId
        songTitle = songSearch['result'][0]['title']
        translation_table = str.maketrans('', '', string.punctuation)
        safeString = songTitle.translate(translation_table)
        fileName = safeString.replace(" ", "") + ".mp3"
    except(httpx.ConnectError, ChunkedEncodingError, ReadError):
        return fileName,songUrl,1
    return fileName,songUrl,0


def insertMetaData(songs,fileLocation):

    try:
        songSearch = VideosSearch(songs, limit=1).result()
        tracks = sp.search(songs)
        ArtWorkURL = tracks['tracks']['items'][0]['album']['images'][0]['url']

        songId = songSearch['result'][0]['id']
        songTitle = songSearch['result'][0]['title']

        response = requests.get(ArtWorkURL)

        albumName = tracks['tracks']['items'][0]['album']['name']
        albumArtistName = tracks['tracks']['items'][0]['album']['artists'][0]['name']
        albumTrackCount = tracks['tracks']['items'][0]['album']['total_tracks']
        albumTrackNumber = tracks['tracks']['items'][0]['track_number']
        releaseDate = tracks['tracks']['items'][0]['album']['release_date'][0:4]
        artistName = tracks['tracks']['items'][0]['artists'][0]['name']
        trackName = tracks['tracks']['items'][0]['name']

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
        print("finished")
        return 0
    except (httpx.ConnectError, ChunkedEncodingError, ReadError,requests.exceptions.ConnectionError, mutagen.aac.AACError):
        print("ConnectionError")
        return 1


