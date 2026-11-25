import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as movieApi from '../api/movies'
import * as accountApi from '../api/auth'

const GENRE_ES = {
  Action: 'Acción', Adventure: 'Aventura', Animation: 'Animación', Comedy: 'Comedia', Crime: 'Crimen',
  Documentary: 'Documental', Drama: 'Drama', Children: 'Familia', Fantasy: 'Fantasía', 'Film-Noir': 'Cine negro',
  Horror: 'Terror', Musical: 'Música', Mystery: 'Misterio', Romance: 'Romance', 'Sci-Fi': 'Ciencia ficción',
  Thriller: 'Suspenso', War: 'Guerra', Western: 'Western',
}

export function mapMovie(movie) {
  if (!movie) return movie
  const genres = (movie.genres || []).map(genre => GENRE_ES[genre] || genre)
  const year = movie.year || Number(movie.title?.match(/\((\d{4})\)$/)?.[1]) || null
  return {
    ...movie,
    id: movie.movie_id ?? movie.id,
    genre: genres[0] || '',
    genres,
    shared_genres: movie.shared_genres?.map(genre => GENRE_ES[genre] || genre),
    year,
    rating: Number(movie.average_rating ?? movie.rating ?? 0),
    poster: movie.poster_url || movie.poster || '',
    posterTitle: movie.title,
  }
}

function readLocal(key, fallback) {
  try { return JSON.parse(localStorage.getItem(key)) ?? fallback } catch { return fallback }
}

export const useMoviesStore = defineStore('movies', () => {
  const username = ref(localStorage.getItem('buscapeli-name') || 'Usuario')
  const user = ref(null)
  const authenticated = ref(false)
  const sessionLoaded = ref(false)
  const catalog = ref([])
  const favorites = ref([])
  const watchlist = ref([])
  const ratedMovies = ref([])
  const ratings = ref(readLocal('buscapeli-ratings', {}))
  const tags = ref(readLocal('buscapeli-tags', {}))
  const savedIds = ref(readLocal('buscapeli-favorites', []))
  const watchIds = ref(readLocal('buscapeli-watchlist', []))
  const loading = ref(false)
  const error = ref('')

  function persist() {
    localStorage.setItem('buscapeli-ratings', JSON.stringify(ratings.value))
    localStorage.setItem('buscapeli-tags', JSON.stringify(tags.value))
    localStorage.setItem('buscapeli-favorites', JSON.stringify(savedIds.value))
    localStorage.setItem('buscapeli-watchlist', JSON.stringify(watchIds.value))
  }
  async function run(action) {
    error.value = ''
    try { return await action() } catch (e) { error.value = e.message; throw e }
  }
  async function fetchMovies(params = {}, append = false) {
    loading.value = true
    try {
      const result = await run(() => movieApi.searchMovies(params))
      catalog.value = append ? [...catalog.value, ...result.items.map(mapMovie)] : result.items.map(mapMovie)
      return { ...result, items: catalog.value }
    } finally { loading.value = false }
  }
  async function fetchPopular(limit = 20) { const result = await run(() => movieApi.getPopular(limit)); return result.items.map(mapMovie) }
  async function fetchTopRated(limit = 20) { const result = await run(() => movieApi.getTopRated(limit)); return result.items.map(mapMovie) }
  async function fetchMovie(id) {
    const result = await run(() => movieApi.getMovieDetails(id))
    return { ...mapMovie(result.movie), ...result, movie: mapMovie(result.movie) }
  }
  async function fetchRecommendations(id, kind = 'collaborative') {
    const result = await run(() => kind === 'genre' ? movieApi.getGenreRecommendations(id) : movieApi.getCollaborativeRecommendations(id))
    return { ...result, movie: mapMovie(result.movie), items: result.items.map(item => mapMovie(item.movie ? { ...item.movie, ...item } : item)) }
  }
  async function fetchForMe() {
    const result = await run(() => movieApi.getPersonalizedRecommendations())
    return { ...result, items: result.items.map(item => mapMovie(item.movie ? { ...item.movie, ...item } : item)) }
  }
  async function loadSession() {
    if (sessionLoaded.value) return authenticated.value ? user.value : null
    try {
      const result = await accountApi.getSession()
      sessionLoaded.value = true
      authenticated.value = result.authenticated
      user.value = result.authenticated ? result : null
      if (result.name) { username.value = result.name; localStorage.setItem('buscapeli-name', result.name) }
      if (result.authenticated) await loadUserCollections()
      return result
    } catch { sessionLoaded.value = true; authenticated.value = false; user.value = null; return null }
  }
  async function loadUserCollections() {
    const [userRatings, saved, watching, userTags] = await Promise.all([
      accountApi.getRatedMovies(), accountApi.getFavorites(), accountApi.getWatchlist(), accountApi.getTags(),
    ])
    ratedMovies.value = userRatings.map(row => mapMovie(row))
    ratings.value = Object.fromEntries(userRatings.map(row => [row.movie_id, Number(row.user_rating)]))
    favorites.value = saved.map(mapMovie)
    watchlist.value = watching.map(mapMovie)
    savedIds.value = saved.map(row => row.movie_id)
    watchIds.value = watching.map(row => row.movie_id)
    tags.value = userTags.reduce((result, row) => {
      result[row.movie_id] ||= []
      result[row.movie_id].push(row.tag)
      return result
    }, {})
    persist()
  }
  async function refreshRatedMovies() {
    const rows = await run(() => accountApi.getRatedMovies())
    ratedMovies.value = rows.map(mapMovie)
    ratings.value = Object.fromEntries(rows.map(row => [row.movie_id, Number(row.user_rating)]))
    persist()
  }
  async function login(email, password) {
    const result = await run(() => accountApi.login({ email, password }))
    authenticated.value = true; user.value = result; username.value = result.name
    sessionLoaded.value = true
    localStorage.setItem('buscapeli-name', result.name)
    await loadUserCollections()
    return result
  }
  async function registerAccount(name, email, password) {
    const result = await run(() => accountApi.register({ name, email, password }))
    authenticated.value = true; user.value = result; username.value = result.name
    sessionLoaded.value = true
    ratings.value = {}; tags.value = {}; favorites.value = []; watchlist.value = []; ratedMovies.value = []; savedIds.value = []; watchIds.value = []
    persist()
    localStorage.setItem('buscapeli-name', result.name)
    return result
  }
  async function logout() {
    if (authenticated.value) await run(() => accountApi.logout())
    authenticated.value = false; user.value = null
    sessionLoaded.value = true
    localStorage.removeItem('buscapeli-name')
    ratings.value = {}; tags.value = {}; favorites.value = []; watchlist.value = []; ratedMovies.value = []; savedIds.value = []; watchIds.value = []
    persist()
  }
  async function rate(id, rating) {
    if (authenticated.value) {
      if (rating <= 0) await run(() => accountApi.deleteRating(id))
      else await run(() => accountApi.saveRating(id, rating))
      await refreshRatedMovies()
    } else {
      const next = { ...ratings.value }
      if (rating <= 0) delete next[id]
      else next[id] = rating
      ratings.value = next
      ratedMovies.value = catalog.value.filter(movie => next[movie.id] > 0)
      persist()
    }
  }
  async function toggleSave(movie) {
    const id = Number(movie.id ?? movie.movie_id)
    const exists = savedIds.value.includes(id)
    savedIds.value = exists ? savedIds.value.filter(value => value !== id) : [...savedIds.value, id]
    favorites.value = exists ? favorites.value.filter(value => value.id !== id) : [...favorites.value, mapMovie(movie)]
    if (authenticated.value) await run(() => exists ? accountApi.removeFavorite(id) : accountApi.addFavorite(id))
    persist()
  }
  async function addTag(id, tag) {
    const clean = tag.trim()
    if (!clean) return
    tags.value = { ...tags.value, [id]: [...new Set([...(tags.value[id] || []), clean])] }
    if (authenticated.value) await run(() => accountApi.addMovieTag(id, clean))
    persist()
  }
  async function toggleWatch(movie) {
    const id = Number(movie.id ?? movie.movie_id)
    const exists = watchIds.value.includes(id)
    watchIds.value = exists ? watchIds.value.filter(value => value !== id) : [...watchIds.value, id]
    watchlist.value = exists ? watchlist.value.filter(value => value.id !== id) : [...watchlist.value, mapMovie(movie)]
    if (authenticated.value) await run(() => exists ? accountApi.removeFromWatchlist(id) : accountApi.addToWatchlist(id))
    persist()
  }

  return { username, user, authenticated, sessionLoaded, catalog, favorites, watchlist, ratedMovies, ratings, tags, savedIds, watchIds, loading, error,
    fetchMovies, fetchPopular, fetchTopRated, fetchMovie, fetchRecommendations, fetchForMe, loadSession,
    login, registerAccount, logout, rate, refreshRatedMovies, toggleSave, addTag, toggleWatch }
})
