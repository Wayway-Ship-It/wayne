import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    user: JSON.parse(localStorage.getItem('user') || '{}'),
    token: localStorage.getItem('token') || ''
  },
  mutations: {
    SET_USER(state, user) {
      state.user = user
      localStorage.setItem('user', JSON.stringify(user))
    },
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    LOGOUT(state) {
      state.user = {}
      state.token = ''
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    },
    UPDATE_USER_INFO(state, userInfo) {
      state.user = { ...state.user, ...userInfo }
      localStorage.setItem('user', JSON.stringify(state.user))
    }
  },
  actions: {
    login({ commit }, loginData) {
      commit('SET_TOKEN', loginData.token)
      commit('SET_USER', {
        userId: loginData.userId,
        username: loginData.username,
        realName: loginData.realName,
        role: loginData.role
      })
    },
    logout({ commit }) {
      commit('LOGOUT')
    }
  },
  getters: {
    isAdmin: state => state.user.role === 'ADMIN'
  }
})
