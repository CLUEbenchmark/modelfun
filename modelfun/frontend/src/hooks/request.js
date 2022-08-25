import { ref } from 'vue';
import useLoading from './loading';
export default function useRequest(
  api=new Promise((resolve, reject) => {}),
  defaultValue = [] ,
  isLoading = true
) {
  const { loading, setLoading } = useLoading(isLoading);
  const response = ref(defaultValue);
  api()
    .then((res) => {
      response.value = res.data
    })
    .finally(() => {
      setLoading(false);
    });
  return { loading, response };
}
