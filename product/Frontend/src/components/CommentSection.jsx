import React, { useEffect, useState } from "react";
import { getComments, postComment, deleteComment } from "../api/api.js";
import "../styles/CommentSection.css";

export default function CommentSection({ bookId, bookProgress }) {
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [replyingTo, setReplyingTo] = useState(null);
  const [replyText, setReplyText] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const currentUserEmail = localStorage.getItem("email");
  const bookCompleted = bookProgress >= 100;

  useEffect(() => {
    fetchComments();
  }, [bookId]);

  async function fetchComments() {
    try {
      const data = await getComments(bookId);
      setComments(data);
    } catch (err) {
      setError("Could not load comments.");
    } finally {
      setLoading(false);
    }
  }

  async function handlePostComment() {
    if (!newComment.trim()) return;
    try {
      await postComment(bookId, newComment.trim());
      setNewComment("");
      fetchComments();
    } catch (err) {
      setError("Failed to post comment.");
    }
  }

  async function handlePostReply(parentCommentId) {
    if (!replyText.trim()) return;
    try {
      await postComment(bookId, replyText.trim(), parentCommentId);
      setReplyText("");
      setReplyingTo(null);
      fetchComments();
    } catch (err) {
      setError("Failed to post reply.");
    }
  }

  async function handleDelete(commentId) {
    try {
      await deleteComment(bookId, commentId);
      fetchComments();
    } catch (err) {
      setError("Failed to delete comment.");
    }
  }

  function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString("en-GB", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    });
  }

  function getUsername(email) {
    return email ? email.split("@")[0] : email;
    }

  return (
    <div className="comment-section-wrapper">

      {/* New comment input */}
      <div className="comment-input-row">
        <input
          className="comment-input"
          placeholder="Write a comment..."
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handlePostComment()}
        />
        <button className="comment-post-btn" onClick={handlePostComment}>
          Post
        </button>
      </div>

      {error && <p className="comment-error">{error}</p>}

      {/* Two column layout */}
      <div className="comment-columns">

        {/* Non-Spoiler Column */}
        <div className="comment-column">
          <h3 className="comment-column-title">Non-Spoiler Comments</h3>

          {loading ? (
            <p className="comment-muted">Loading comments...</p>
          ) : comments.length === 0 ? (
            <p className="comment-muted">No comments yet. Be the first!</p>
          ) : (
            comments.map((comment) => (
              <div key={comment.id} className="comment-card">

                <div className="comment-header">
                  <span className="comment-author">{getUsername(comment.authorEmail)}</span>
                  <span className="comment-date">{formatDate(comment.createdAt)}</span>
                </div>

                <p className="comment-content">{comment.content}</p>

                <div className="comment-actions">
                  <span className="comment-like">♥ {comment.likeCount}</span>
                  <button
                    className="comment-action-btn"
                    onClick={() => {
                      setReplyingTo(comment.id);
                      setReplyText("");
                    }}
                  >
                    Reply
                  </button>
                  {comment.authorEmail === currentUserEmail && (
                    <button
                      className="comment-action-btn delete"
                      onClick={() => handleDelete(comment.id)}
                    >
                      Delete
                    </button>
                  )}
                </div>

                {/* Reply input */}
                {replyingTo === comment.id && (
                  <div className="reply-input-row">
                    <input
                      className="reply-input"
                      placeholder={`Reply to ${getUsername(comment.authorEmail)}...`}
                      value={replyText}
                      onChange={(e) => setReplyText(e.target.value)}
                      onKeyDown={(e) => e.key === "Enter" && handlePostReply(comment.id)}
                      autoFocus
                    />
                    <button
                      className="reply-post-btn"
                      onClick={() => handlePostReply(comment.id)}
                    >
                      Reply
                    </button>
                    <button
                      className="reply-cancel-btn"
                      onClick={() => setReplyingTo(null)}
                    >
                      Cancel
                    </button>
                  </div>
                )}

                {/* Replies */}
                {comment.replies && comment.replies.length > 0 && (
                  <div className="replies-block">
                    {comment.replies.map((reply) => (
                      <div key={reply.id} className="reply-card">
                        <div className="comment-header">
                          <span className="comment-author">{getUsername(reply.authorEmail)}</span>
                          <span className="comment-date">{formatDate(reply.createdAt)}</span>
                        </div>
                        <p className="comment-content">{reply.content}</p>
                        <div className = "comment-actions">
                            {reply.authorEmail !== currentUserEmail && (
                                <button
                                className="comment-action-btn"
                                onClick={() => {
                                    setReplyingTo(comment.id); // parent comment id, not reply id
                                    setReplyText(`@${getUsername(reply.authorEmail)} `); // pre-fill with mention
                                }}
                                >
                                Reply
                                </button>
                            )}
                        {reply.authorEmail === currentUserEmail && (
                          <button
                            className="comment-action-btn delete"
                            onClick={() => handleDelete(reply.id)}
                          >
                            Delete
                          </button>
                        )}
                      </div>
                    </div>
                    ))}
                  </div>
                )}
              </div>
            ))
          )}
        </div>

        {/* Spoiler Column */}
        <div className="comment-column">
          <h3 className="comment-column-title">
            Spoiler Comments <span>🔒</span>
          </h3>
          {bookCompleted ? (
            <p className="comment-muted">Spoiler comments coming soon.</p>
          ) : (
            <div className="comment-locked-box">
              <p className="comment-locked-text">Complete book to unlock</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}