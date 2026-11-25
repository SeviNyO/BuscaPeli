
import { createRouter, createWebHistory } from 'vue-router'

import MainLayout from '../layouts/MainLayout.vue'

import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import OnboardingView from '../views/OnboardingView.vue'
import HomeView from '../views/HomeView.vue'
import RecommendedView from '../views/RecommendedView.vue'
import DiscoverView from '../views/DiscoverView.vue'
import MovieDetailView from '../views/MovieDetailView.vue'
import MovieRecommendationsView from '../views/MovieRecommendationsView.vue'
import MyListsView from '../views/MyListsView.vue'
import { useMoviesStore } from '../stores/movies'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/onboarding',
      name: 'onboarding',
      component: OnboardingView
    },
    {
      path: '/app',
      component: MainLayout,

      children: [
        {
          path: '',
          redirect: '/app/home'
        },
        {
          path: 'home',
          name: 'home',
          component: HomeView
        },
        {
          path: 'recommended',
          name: 'recommended',
          component: RecommendedView
        },
        {
          path: 'discover',
          name: 'discover',
          component: DiscoverView
        },
        {
          path: 'movies/:id',
          name: 'movie-detail',
          component: MovieDetailView
        },
        {
          path: 'movies/:id/recommendations',
          name: 'movie-recommendations',
          component: MovieRecommendationsView
        },
        {
          path: 'lists',
          name: 'my-lists',
          component: MyListsView
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ],

  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach(async to => {
  const store = useMoviesStore()
  if (to.path.startsWith('/app') || to.path === '/onboarding') {
    if (!store.sessionLoaded) await store.loadSession()
    if (!store.authenticated) return '/login'
  }
  if ((to.path === '/login' || to.path === '/register') && store.authenticated) return '/app/home'
})

export default router
