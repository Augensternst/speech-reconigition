import $ from 'jquery'

export default {
    state: {
        id: "",
        username: "",
        avatar: "",
        token: "",
        is_login: false,
    },
    getters: {
    },
    mutations: {
        updateUser(state, user) {
            state.id = user.id
            state.username = user.username
            state.avatar = user.avatar
            state.is_login = user.is_login
        },
        updateToken(state, token) {
            state.token = token
            console.log("updateToken", token)
        },
        logout(state){
            state.id = ""
            state.username = ""
            state.avatar = ""
            state.token = ""
            state.is_login = false
        }
    },
    actions: {
        login(context, data) {
            $.ajax({
                url: "http://localhost:3001/user/account/token/",
                type: "post",
                data: {
                    username: data.username,
                    password: data.password
                },
                success(resp) {
                    if (resp.error_msg === "success") {
                        localStorage.setItem("jwt_token", resp.token)
                        context.commit("updateToken", resp.token)
                        data.success()
                    } else {
                        data.error()
                    }
                },
                error() {
                    data.error()
                }
            })
        },
        getInfo(context, data) {
            $.ajax({
                url: "http://localhost:3001/user/account/info/",
                type: "get",
                headers: {
                    Authorization: "Bearer " + context.state.token
                },
                success(resp) {
                    if (resp.error_msg === "success") {
                        context.commit("updateUser", {
                            ...resp,
                            is_login: true
                        })
                        data.success(resp)
                    }
                },
            })
        },
        logout(context){
            context.commit("logout")
            localStorage.removeItem("jwt_token")
        },
    },
    modules: {
    }
}
