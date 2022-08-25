<script lang="jsx">
  import { defineComponent, ref, h, compile, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { store } from '@/store';
  import { listenerRouteChange } from '@/utils/route-listener';

  export default defineComponent({
    emit: ['collapse'],
    setup() {
      const router = useRouter();
      const collapsed = computed({
        get() {
          return store.getters.menuCollapse
        },
        set(value) {
          store.commit('app/SET_MENU_COLLAPSE', value);
        },
      });
      const appRoute = computed(() => {
        return router
          .getRoutes()
          .find((el) => el.name === 'layout');
      });
      const menuTree = computed(() => {
        const copyRouter = JSON.parse(JSON.stringify(appRoute.value.children));
        copyRouter.sort(
          (a, b) => {
            return (a.meta.order || 0) - (b.meta.order || 0);
          }
        );
        function travel(_routes, layer) {
          if (!_routes) return null;
          const collector = _routes.map((element) => {
            // leaf node
            if (!element.children) {
              return element;
            }
            // route filter hideInMenu true
            element.children = element.children.filter(
              (x) => x.meta?.hideInMenu !== true
            );
            // Associated child node
            const subItem = travel(element.children, layer);
            if (subItem.length) {
              element.children = subItem;
              return element;
            }
            // the else logic
            if (layer > 1) {
              element.children = subItem;
              return element;
            }
            if (element.meta?.hideInMenu === false) {
              return element;
            }
            return null;
          });
          return collector.filter(Boolean);
        }
        return travel(copyRouter, 0);
      });
      const selectedKey = ref([]);
      const goto = (item) => {
        router.push({
          name: item.name,
        });
      };
      listenerRouteChange((newRoute) => {
        if (newRoute.meta.requiresAuth && !newRoute.meta.hideInMenu) {
          const key = newRoute?.name ;
          selectedKey.value = [key];
        }
      }, true);
      const setCollapse = (val) => {
        store.commit('app/SET_MENU_COLLAPSE', val);
      };

      const renderSubMenu = () => {
        function travel(_route, nodes = []) {
          if (_route) {
            _route.forEach((element) => {
              const icon = element?.meta?.icon
                ? `<${element?.meta?.icon}/>`
                : ``;
              const r = (
                element.children?.length>0?
                <a-sub-menu
                  key={element?.name}
                  v-slots={{
                    icon: () => h(compile(icon)),
                    title: () => h(compile(element?.meta?.locale || '')),
                  }}
                >
                  {element?.children?.map((elem) => {
                    return (
                      <a-menu-item key={elem.name} onClick={() => goto(elem)}>
                        {elem?.meta?.locale || ''}
                        {travel(elem.children ?? [])}
                      </a-menu-item>
                    );
                  })}
                </a-sub-menu>:
                <a-menu-item key={element.name} onClick={() => goto(element)} v-slots={{
                    icon: () => h(compile(icon)),
                  }}>
                        {element?.meta?.locale || ''}
                        {travel(element.children ?? [])}
                </a-menu-item>
              );
              nodes.push(r );
            });
          }
          return nodes;
        }
        return travel(menuTree.value);
      };
      return () => (
        <a-menu
          v-model:collapsed={collapsed.value}
          show-collapse-button={true}
          auto-open={false}
          selected-keys={selectedKey.value}
          auto-open-selected={true}
          level-indent={34}
          style="height: 100%"
          onCollapse={setCollapse}
        >
          {renderSubMenu()}
        </a-menu>
      );
    },
  });
</script>

<style lang="less" scoped>
:deep(.arco-menu-inner) {
    .arco-menu-inline-header {
        display: flex;
        align-items: center;
    }

    .arco-icon {
        &:not(.arco-icon-down) {
            font-size: 18px;
        }
    }
}
</style>
