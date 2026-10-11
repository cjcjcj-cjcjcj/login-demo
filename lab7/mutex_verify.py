# -*- coding: utf-8 -*-
"""
实验七  并发系统安全性与活性验证：互斥锁协议的状态空间搜索

模型：两个进程共享临界区，全局状态 = (p1状态, p2状态, turn)
  p1, p2 ∈ {N(非临界), R(请求), C(临界)}
  turn   ∈ {1, 2}        —— 共享变量，指示双方竞争时谁可优先进入

迁移规则（每个进程）：
  N -> R : 总是使能（进程发起进入临界区的请求）
  R -> C : 正确版本：对方不在 C 且（对方不在 R 或 turn == 自己）
           错误版本：（对方不在 R 或 turn == 自己）
                    —— 故意缺少"对方不在 C"这一检查条件
  C -> N : 总是使能，退出时置 turn := 对方

验证内容：
  1) BFS 生成全部可达全局状态，记录前驱以恢复反例路径
  2) 互斥性：不存在可达状态使 p1=C 且 p2=C；若违反输出最短反例路径
  3) 无死锁性：任何可达（非终止）状态至少存在一个使能动作
"""

from collections import deque

N, R, C = 'N', 'R', 'C'
INIT = (N, N, 1)          # 初始状态：两进程均处于非临界区，turn = 1


def transitions(state, version):
    """返回 [(后继状态, 动作描述), ...]"""
    p1, p2, turn = state
    succ = []

    # ---------- 进程 P1 的动作 ----------
    if p1 == N:
        succ.append(((R, p2, turn), 'P1: N->R（请求进入临界区）'))
    elif p1 == R:
        if version == 'correct':
            enabled = (p2 != C) and (p2 != R or turn == 1)
        else:  # buggy：缺少“对方不在 C”的检查
            enabled = (p2 != R or turn == 1)
        if enabled:
            succ.append(((C, p2, turn), 'P1: R->C（进入临界区）'))
    else:  # p1 == C
        succ.append(((N, p2, 2), 'P1: C->N（退出临界区，turn:=2）'))

    # ---------- 进程 P2 的动作 ----------
    if p2 == N:
        succ.append(((p1, R, turn), 'P2: N->R（请求进入临界区）'))
    elif p2 == R:
        if version == 'correct':
            enabled = (p1 != C) and (p1 != R or turn == 2)
        else:  # buggy：缺少“对方不在 C”的检查
            enabled = (p1 != R or turn == 2)
        if enabled:
            succ.append(((p1, C, turn), 'P2: R->C（进入临界区）'))
    else:  # p2 == C
        succ.append(((p1, N, 1), 'P2: C->N（退出临界区，turn:=1）'))

    return succ


def bfs(version):
    """
    BFS 状态空间搜索。
    返回 (reachable, pred, violation, deadlock_states, edge_count)
      reachable        : 可达状态集合（BFS 访问顺序列表）
      pred             : 前驱字典  state -> (前驱状态, 动作)
      violation        : 第一个违反互斥性的状态（无则 None）
      deadlock_states  : 无任何使能动作的非终止状态列表
    """
    visited = [INIT]
    seen = {INIT}
    pred = {INIT: None}
    q = deque([INIT])
    violation = None
    deadlock_states = []
    edge_count = 0

    while q:
        s = q.popleft()
        # 互斥性检查
        if s[0] == C and s[1] == C:
            violation = s
            break
        succ = transitions(s, version)
        edge_count += len(succ)
        # 无死锁性检查：状态 s 非终止却无任何使能动作
        if not succ:
            deadlock_states.append(s)
        for ns, act in succ:
            if ns not in seen:
                seen.add(ns)
                visited.append(ns)
                pred[ns] = (s, act)
                q.append(ns)

    return visited, pred, violation, deadlock_states, edge_count


def full_bfs(version):
    """完整探索（不因互斥违反而中断），用于统计与无死锁检查"""
    visited = [INIT]
    seen = {INIT}
    pred = {INIT: None}
    q = deque([INIT])
    deadlock_states = []
    edge_count = 0
    mutex_violations = []

    while q:
        s = q.popleft()
        if s[0] == C and s[1] == C:
            mutex_violations.append(s)
        succ = transitions(s, version)
        edge_count += len(succ)
        if not succ:
            deadlock_states.append(s)
        for ns, act in succ:
            if ns not in seen:
                seen.add(ns)
                visited.append(ns)
                pred[ns] = (s, act)
                q.append(ns)

    return visited, pred, mutex_violations, deadlock_states, edge_count


def path_to(pred, target):
    """由前驱链恢复从初始状态到 target 的最短路径"""
    path = []
    s = target
    while s is not None:
        entry = pred[s]
        path.append((s, entry[1] if entry else '（初始状态）'))
        s = entry[0] if entry else None
    path.reverse()
    return path


def fmt(s):
    return f'(P1={s[0]}, P2={s[1]}, turn={s[2]})'


def report(version, title):
    print('=' * 64)
    print(title)
    print('=' * 64)

    # 互斥性：标准 BFS，首次发现违反即停止（得到最短反例）
    _, pred, violation, _, _ = bfs(version)

    # 完整探索：状态空间统计 + 无死锁检查
    visited, fpred, violations, deadlock_states, edges = full_bfs(version)
    print(f'可达全局状态数：{len(visited)}    迁移边数：{edges}')
    print('可达状态列表（BFS 访问顺序）：')
    for i, s in enumerate(visited):
        print(f'  s{i:<2} = {fmt(s)}')

    print('-' * 64)
    print('[性质 1] 互斥性：任意可达状态不满足 P1=C 且 P2=C')
    if violation is None:
        print('  结论：成立。全部可达状态中不存在 P1=C 且 P2=C 的状态。')
    else:
        print(f'  结论：不成立！违反状态 {fmt(violation)}')
        print('  最短反例路径：')
        for s, act in path_to(fpred if violation in fpred else pred, violation):
            print(f'    {fmt(s)}   <-- {act}')

    print('-' * 64)
    print('[性质 2] 无死锁性：任何非终止状态至少存在一个使能动作')
    if not deadlock_states:
        print('  结论：成立。所有可达状态均有至少一个使能动作，无死锁状态。')
    else:
        print(f'  结论：不成立！死锁状态：{[fmt(s) for s in deadlock_states]}')
    print()


if __name__ == '__main__':
    report('correct', '【正确版本】进入临界区前检查：对方不在 C 且（对方不在 R 或轮到自己）')
    report('buggy',   '【错误版本】进入条件缺少“对方不在 C”的检查（只看对方是否请求与 turn）')
