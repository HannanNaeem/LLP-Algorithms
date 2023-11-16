// ******* AUTOMATICALLY GENERATED BY PyKokkos *******
#ifndef PK_FUNCTOR_LLP_BORUVKA_HPP
#define PK_FUNCTOR_LLP_BORUVKA_HPP

template <class ExecSpace>struct pk_functor_llp_boruvka{struct llp_boruvka_tag{};int32_t dim;Kokkos::View<int32_t*,Kokkos::LayoutLeft,typename ExecSpace::memory_space,Kokkos::Experimental::DefaultViewHooks> sol_view;Kokkos::View<int32_t*,Kokkos::LayoutLeft,typename ExecSpace::memory_space,Kokkos::Experimental::DefaultViewHooks> forbidden_view; pk_functor_llp_boruvka(int32_t dim, Kokkos::View<int32_t*,Kokkos::LayoutLeft,typename ExecSpace::memory_space,Kokkos::Experimental::DefaultViewHooks> sol_view, Kokkos::View<int32_t*,Kokkos::LayoutLeft,typename ExecSpace::memory_space,Kokkos::Experimental::DefaultViewHooks> forbidden_view, int32_t pk_randpool_num_states, int32_t pk_randpool_seed){ this->dim= dim;this->sol_view= sol_view;this->forbidden_view= forbidden_view; }; pk_functor_llp_boruvka(int32_t dim, Kokkos::View<int32_t*,Kokkos::LayoutLeft,typename ExecSpace::memory_space,Kokkos::Experimental::DefaultViewHooks> sol_view, Kokkos::View<int32_t*,Kokkos::LayoutLeft,typename ExecSpace::memory_space,Kokkos::Experimental::DefaultViewHooks> forbidden_view){ this->dim= dim;this->sol_view= sol_view;this->forbidden_view= forbidden_view; };KOKKOS_FUNCTION void operator()(const llp_boruvka_tag& , int32_t tid)const{ int32_t exists_forbidden= 0;for (int32_t i= 0; (i < dim); (i += 1)){ if ((forbidden_view(i) == 1)){ exists_forbidden= 1;break; } }Kokkos::fence();while(exists_forbidden) { forbidden_view(tid)= 0;if ((sol_view(tid) != sol_view(sol_view(tid)))){ forbidden_view(tid)= 1; }Kokkos::fence();if (forbidden_view(tid)){ sol_view(tid)= sol_view(sol_view(tid)); }exists_forbidden= 0;for (int32_t i= 0; (i < dim); (i += 1)){ if ((forbidden_view(i) == 1)){ exists_forbidden= 1;break; } }Kokkos::fence(); } };};

#endif