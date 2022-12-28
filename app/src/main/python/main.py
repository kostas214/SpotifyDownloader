from pytube import YouTube
import spotipy
from spotipy.oauth2 import SpotifyClientCredentials
import uyts
import string
import music_tag
import requests
import SpotifyApiCredentials
import time


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
    links = []
    tracks = sp.playlist_items(playlist_id=playlistLink, offset=offset)
    for key in tracks['items']:
        songs.append(f"{key['track']['name']} {key['track']['artists'][0]['name']}")
        links.append(key['track']['external_urls']['spotify'])
    while done:
        if len(songs) == offset + 100:
            tracks = sp.playlist_items(playlist_id=playlistLink, offset=offset)
            for key in tracks['items']:
                songs.append(f"{key['track']['name']} {key['track']['artists'][0]['name']}")
                #links.append(key['track']['external_urls']['spotify'])
            offset += 100
        if len(songs) < offset + 100:
            done = False

    return songs,links





def DownloadSongs(songs,links,filePath,index):
    urlTemplateForServer = "http://192.168.2.19:5000/?link="
    songId = uyts.Search(songs)
    result = "https://www.youtube.com/watch?v=" + songId.results[0].id
    translation_table = str.maketrans('', '', string.punctuation)
    safeString = songId.results[0].title
    safeString = safeString.translate(translation_table)
    fileName = safeString.replace(" ", "") + ".aac"

    tracks = sp.track(track_id=links)

    ArtWorkURL = tracks['album']['images'][0]['url']
    albumName = tracks['album']['name']
    albumArtistName = tracks['album']['artists'][0]['name']
    albumTrackCount = tracks['album']['total_tracks']
    albumTrackNumber = tracks['track_number']
    releaseDate = tracks['album']['release_date'][0:4]
    artistName = tracks['artists'][0]['name']
    trackName = tracks['name']




    st = time.time()

    urlForServer = urlTemplateForServer + songId.results[0].id
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







