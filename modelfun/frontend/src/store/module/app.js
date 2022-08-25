

const state = {
    "menu": true,
    "menuCollapse": false,
    "footer": true,
    "themeColor": "#165DFF",
    "menuWidth": 220,
    "globalSettings": false,
    "device": "desktop"
}

const mutations = {
    SET_MENU_COLLAPSE: (state, bol) => {
        state.menuCollapse = bol
    }
}

const actions = {
}

export default {
    namespaced: true,
    state,
    mutations,
    actions
}