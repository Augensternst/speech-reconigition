import {useCookies} from "@vueuse/integrations/useCookies";

const TokenKey = 'user-token';

export const useAuth = () => {
  const {get, set, remove} = useCookies();

  const getToken = () => get(TokenKey);
  const setToken = (token: string) => set(TokenKey, token);
  const removeToken = () => remove(TokenKey);
  const logout =()=>{
    removeToken();
  }

  return {
    getToken,
    setToken,
    removeToken,
    logout
  }
}