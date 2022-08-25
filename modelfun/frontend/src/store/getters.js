
const getters = {
  menuCollapse:state => state.app.menuCollapse,
  menuWidth:state => state.app.menuWidth,
  avatar:state => state.user.avatar,
  userInfo:state => state.user.user,
  cantShowFlag:state => state.user.cantShowFlag,
  cantShowList:state => state.user.cantShowList
}
export default getters
