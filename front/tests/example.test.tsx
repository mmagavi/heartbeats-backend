import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Header from "../src/components/Header";

/*
 * This is an example test file.
 * It is meant to be a starting point for writing your own tests.
 * Feel free to research all the other functions that Jest and Testing Library provide!
 */

describe("stencil code example", () => {
  test("basic math", () => {
    expect(1 + 1).toBe(2);
  });

  test("loads and displays header", async () => {
    render(<Header />);
    expect(screen.getByText(/heartBeats/i)).toBeInTheDocument();
  });
});
