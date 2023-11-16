// ******* AUTOMATICALLY GENERATED BY PyKokkos *******
#ifndef PK_FUNCTOR_REDUCE_EDGES_HPP
#define PK_FUNCTOR_REDUCE_EDGES_HPP

template <class ExecSpace>struct pk_functor_reduce_edges{struct reduce_edges_tag{};int32_t dim;Kokkos::View<int32_t*,Kokkos::LayoutRight,typename ExecSpace::memory_space,Kokkos::Experimental::DefaultViewHooks> sol_view;Kokkos::View<int64_t**,Kokkos::LayoutRight,Kokkos::HostSpace,Kokkos::Experimental::DefaultViewHooks> graph; pk_functor_reduce_edges(int32_t dim, Kokkos::View<int32_t*,Kokkos::LayoutRight,typename ExecSpace::memory_space,Kokkos::Experimental::DefaultViewHooks> sol_view, Kokkos::View<int64_t**,Kokkos::LayoutRight,Kokkos::HostSpace,Kokkos::Experimental::DefaultViewHooks> graph, int32_t pk_randpool_num_states, int32_t pk_randpool_seed){ this->dim= dim;this->sol_view= sol_view;this->graph= graph; }; pk_functor_reduce_edges(int32_t dim, Kokkos::View<int32_t*,Kokkos::LayoutRight,typename ExecSpace::memory_space,Kokkos::Experimental::DefaultViewHooks> sol_view, Kokkos::View<int64_t**,Kokkos::LayoutRight,Kokkos::HostSpace,Kokkos::Experimental::DefaultViewHooks> graph){ this->dim= dim;this->sol_view= sol_view;this->graph= graph; };KOKKOS_FUNCTION void operator()(const reduce_edges_tag& , int32_t tid)const{ int32_t v= ((double)((tid)) / (dim));int32_t w= ((tid) - (((v) * (dim))));if ((sol_view(v) == sol_view(w))){ graph(v,w)= 0; } };};

#endif