import { api } from './client'

export const searchMovies = (params = {}) => {
  const query = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) if (value !== '' && value != null) query.set(key, value)
  return api(`/movies?${query}`)
}
export const getMovie = id => api(`/movies/${id}`)
export const getMovieDetails = id => api(`/movies/${id}/details`)
export const getPoster = id => api(`/movies/${id}/poster`)
export const getPopular = limit => api(`/movies/popular?limit=${limit}`)
export const getTopRated = (limit, minRatings = 20) => api(`/movies/top-rated?limit=${limit}&minRatings=${minRatings}`)
export const getCollaborativeRecommendations = id => api(`/recommendations/movies/${id}`)
export const getGenreRecommendations = id => api(`/recommendations/movies/${id}/by-genre`)
export const getPersonalizedRecommendations = () => api('/recommendations/me')
