# RiftGuard V0.7.0 — systems audit

Functional UI added to the Home kingdom activity hub with five complete offline menus: Daily Rewards, Daily Quests, Weekly Events, Achievements and permanent Supply Shop.

All rewards go directly into the existing `supplies` campaign currency. Daily/quest/event and achievement claims are guarded by persistent flags to prevent duplicate awards. Quests track kills, wins and Pulse uses only after the live battle ends. Event rotation resets by seven-day UTC week. Achievements count only gameplay result events. The four Shop upgrades have real combat effects: hero HP, core HP, turret damage and skill cooldown.

Automated tests cover actual taps and screenshot captures as well as persistent state and real battle statistics. Visual fidelity and full asset generation remain separate unfinished work.
