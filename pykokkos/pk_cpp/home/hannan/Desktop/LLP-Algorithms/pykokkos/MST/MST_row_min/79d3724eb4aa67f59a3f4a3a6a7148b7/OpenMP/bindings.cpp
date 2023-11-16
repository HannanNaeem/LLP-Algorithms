// ******* AUTOMATICALLY GENERATED BY PyKokkos *******
#include <pybind11/pybind11.h>
#include <Kokkos_Core.hpp>
#include <Kokkos_Random.hpp>
#include <Kokkos_Sort.hpp>
#include <fstream>
#include <iostream>
#include <cmath>
#include <functor.hpp>
#include <functor_cast.hpp>

void run_row_min(int32_t dim,Kokkos::View<int32_t*,Kokkos::LayoutRight,pk_arg_memspace,Kokkos::Experimental::DefaultViewHooks> mins_view,Kokkos::View<int64_t**,Kokkos::LayoutRight,Kokkos::HostSpace,Kokkos::Experimental::DefaultViewHooks> view2d,pk_exec_space pk_exec_space_instance,const std::string& pk_kernel_name,int pk_threads_begin,int pk_threads_end,int pk_randpool_seed,int pk_randpool_num_states) {  auto pk_d_mins_view = Kokkos::create_mirror_view_and_copy(pk_exec_space_instance, mins_view);auto pk_d_view2d = view2d;pk_functor_row_min<pk_exec_space> pk_f(dim,pk_d_mins_view,pk_d_view2d,pk_randpool_seed,pk_randpool_num_states); Kokkos::parallel_for(pk_kernel_name,Kokkos::RangePolicy<pk_exec_space,pk_functor_row_min<pk_exec_space>::row_min_tag>(pk_exec_space_instance, pk_threads_begin,pk_threads_end),pk_f);Kokkos::resize(mins_view,pk_d_mins_view.extent(0));Kokkos::deep_copy(mins_view, pk_d_mins_view); }
void wrapper_row_min(pybind11::kwargs kwargs) {run_row_min(kwargs["dim"].cast<int32_t>(),kwargs["mins_view"].cast<Kokkos::View<int32_t*,Kokkos::LayoutRight,pk_arg_memspace,Kokkos::Experimental::DefaultViewHooks>>(),kwargs["view2d"].cast<Kokkos::View<int64_t**,Kokkos::LayoutRight,Kokkos::HostSpace,Kokkos::Experimental::DefaultViewHooks>>(),kwargs["pk_exec_space_instance"].cast<pk_exec_space>(),kwargs["pk_kernel_name"].cast<std::string>(),kwargs["pk_threads_begin"].cast<int>(),kwargs["pk_threads_end"].cast<int>(),kwargs["pk_randpool_seed"].cast<int>(),kwargs["pk_randpool_num_states"].cast<int>());;}
PYBIND11_MODULE(pk_cpp_home_hannan_Desktop_LLP_Algorithms_pykokkos_MST_MST_row_min_79d3724eb4aa67f59a3f4a3a6a7148b7_OpenMP_kernel_cpython_311_x86_64_linux_gnu_so, k) {k.def("wrapper_row_min", &wrapper_row_min);}