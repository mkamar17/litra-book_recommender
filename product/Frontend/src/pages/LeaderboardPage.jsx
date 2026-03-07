import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from "recharts";
import NavBar from "../components/NavBar.jsx";
import "../styles/LeaderboardPage.css";
import { getLeaderboard } from "../api/api.js";

const PERIODS = ["WEEKLY", "MONTHLY", "ALL_TIME"];
const PERIOD_LABELS = { WEEKLY: "Weekly", MONTHLY: "Monthly", ALL_TIME: "All Time" };

const RANK_COLORS = ["#FFD700", "#C0C0C0", "#CD7F32"];
const BAR_COLORS = ["#e63946", "#ff6b6b", "#ff8fa3", "#c9184a", "#a4133c"];

function getRankEmoji(index) {
  if (index === 0) return "🥇";
  if (index === 1) return "🥈";
  if (index === 2) return "🥉";
  return null;
}

function getUsername(email) {
  return email ? email.split("@")[0] : email;
}

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div className="lb-tooltip">
        <p className="lb-tooltip-name">{label}</p>
        <p className="lb-tooltip-value">{payload[0].value} pts</p>
      </div>
    );
  }
  return null;
};

export default function LeaderboardPage() {
  const navigate = useNavigate();
  const [period, setPeriod] = useState("WEEKLY");
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const currentUserEmail = localStorage.getItem("email");

  useEffect(() => {
    fetchLeaderboard();
  }, [period]);

  async function fetchLeaderboard() {
    setLoading(true);
    setError(null);
    try {
      const data = await getLeaderboard(period);
      setEntries(data);
    } catch (err) {
      setError("Could not load leaderboard.");
    } finally {
      setLoading(false);
    }
  }

  const chartData = entries.slice(0, 8).map((e) => ({
    name: getUsername(e.email),
    points: e.points,
  }));

  return (
    <div className="lb-page">
      <NavBar />

      <div className="lb-container">
        {/* Header */}
        <div className="lb-header">
          <h1 className="lb-title">🏆 Leaderboard</h1>
          <p className="lb-subtitle">See how you stack up against your friends</p>

          {/* Period tabs */}
          <div className="lb-tabs">
            {PERIODS.map((p) => (
              <button
                key={p}
                className={`lb-tab ${period === p ? "lb-tab--active" : ""}`}
                onClick={() => setPeriod(p)}
              >
                {PERIOD_LABELS[p]}
              </button>
            ))}
          </div>
        </div>

        {error && <p className="lb-error">{error}</p>}

        {loading ? (
          <p className="lb-loading">Loading...</p>
        ) : entries.length === 0 ? (
          <div className="lb-empty">
            <p>No friends on the leaderboard yet.</p>
            <p className="lb-empty-sub">Add friends to see how you compare!</p>
          </div>
        ) : (
          <div className="lb-content">
            {/* Table */}
            <div className="lb-table-wrapper">
              <table className="lb-table">
                <thead>
                  <tr>
                    <th>Rank</th>
                    <th>Reader</th>
                    <th>Points</th>
                    <th>Pages Read</th>
                    <th>Books</th>
                  </tr>
                </thead>
                <tbody>
                  {entries.map((entry, index) => (
                    <tr
                      key={entry.email}
                      className={`lb-row ${index < 3 ? "lb-row--top" : ""} ${entry.email === currentUserEmail ? "lb-row--you" : ""}`}
                      style={{ animationDelay: `${index * 0.05}s` }}
                    >
                      <td className="lb-rank">
                        {getRankEmoji(index) ? (
                          <span className="lb-rank-emoji">{getRankEmoji(index)}</span>
                        ) : (
                          <span
                            className="lb-rank-num"
                            style={{ color: RANK_COLORS[index] || "rgba(255,255,255,0.5)" }}
                          >
                            {String(index + 1).padStart(2, "0")}
                          </span>
                        )}
                      </td>
                      <td className="lb-player">
                        <div className="lb-avatar">
                          {getUsername(entry.email)[0].toUpperCase()}
                        </div>
                        <span className="lb-username">
                          {entry.email === currentUserEmail ? "You" : getUsername(entry.email)}
                        </span>
                      </td>
                      <td className="lb-points">
                        <span className="lb-points-badge">{entry.points.toLocaleString()}</span>
                      </td>
                      <td className="lb-stat">{entry.pagesRead.toLocaleString()}</td>
                      <td className="lb-stat">{entry.booksCompleted}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Chart */}
            <div className="lb-chart-wrapper">
              <h3 className="lb-chart-title">Points Comparison</h3>
              <ResponsiveContainer width="100%" height={340}>
                <BarChart data={chartData} margin={{ top: 10, right: 10, left: -10, bottom: 40 }}>
                  <XAxis
                    dataKey="name"
                    tick={{ fill: "rgba(255,255,255,0.5)", fontSize: 11 }}
                    axisLine={false}
                    tickLine={false}
                    angle={-35}
                    textAnchor="end"
                  />
                  <YAxis
                    tick={{ fill: "rgba(255,255,255,0.35)", fontSize: 10 }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <Tooltip content={<CustomTooltip />} cursor={{ fill: "rgba(255,255,255,0.04)" }} />
                  <Bar dataKey="points" radius={[6, 6, 0, 0]}>
                    {chartData.map((_, index) => (
                      <Cell
                        key={index}
                        fill={BAR_COLORS[index % BAR_COLORS.length]}
                        opacity={index === 0 ? 1 : 0.75}
                      />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}